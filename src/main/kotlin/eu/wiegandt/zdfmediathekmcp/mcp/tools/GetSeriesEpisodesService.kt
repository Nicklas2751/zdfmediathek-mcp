package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.model.*
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.graphql.client.GraphQlClientException
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.stereotype.Service

@Service
class GetSeriesEpisodesService(private val zdfGraphQlClient: HttpGraphQlClient) {

    private val logger = LoggerFactory.getLogger(GetSeriesEpisodesService::class.java)

    companion object {
        // Query for episodes with sorting support and pageInfo. We request pageInfo for episodes and also for season-embedded episodes.
        // Build a GraphQL query that uses a variable for the sort direction (OrderByDirection).
        // We keep the sort field inline (EDITORIAL_DATE or EPISODE_NUMBER) because the server expects enum tokens.
        // Use a full input variable for sortBy so enums can be passed via JSON variables.
        // We reuse the VideosConnectionSortByInput input type here since episodes return Video nodes.
        private fun buildQuery(includeSort: Boolean) = if (includeSort) {
            """
            query GetSeriesEpisodes(${'$'}query: String!, ${'$'}first: Int, ${'$'}after: Cursor, ${'$'}sortBy: [VideosConnectionSortByInput!]) {
              searchDocuments(query: ${'$'}query, first: ${'$'}first) {
                 results {
                    item {
                        __typename
                        ... on DefaultNoSectionsSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after, sortBy: ${'$'}sortBy) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                        ... on DefaultWithSectionsSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after, sortBy: ${'$'}sortBy) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                        ... on SeasonSeriesSmartCollection {
                            title
                            seasons {
                                nodes {
                                    episodes(first: ${'$'}first, after: ${'$'}after, sortBy: ${'$'}sortBy) {
                                        nodes {
                                            title
                                            editorialDate
                                            sharingUrl
                                            episodeInfo {
                                                seasonNumber
                                                episodeNumber
                                            }
                                        }
                                        pageInfo { hasNextPage endCursor }
                                    }
                                }
                            }
                        }
                        ... on MiniSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after, sortBy: ${'$'}sortBy) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                        ... on EndlessSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after, sortBy: ${'$'}sortBy) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                    }
                 }
              }
            }
        """
        } else {
            """
            query GetSeriesEpisodes(${'$'}query: String!, ${'$'}first: Int, ${'$'}after: Cursor) {
              searchDocuments(query: ${'$'}query, first: ${'$'}first) {
                 results {
                    item {
                        __typename
                        ... on DefaultNoSectionsSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                        ... on DefaultWithSectionsSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                        ... on SeasonSeriesSmartCollection {
                            title
                            seasons {
                                nodes {
                                    episodes(first: ${'$'}first, after: ${'$'}after) {
                                        nodes {
                                            title
                                            editorialDate
                                            sharingUrl
                                            episodeInfo {
                                                seasonNumber
                                                episodeNumber
                                            }
                                        }
                                        pageInfo { hasNextPage endCursor }
                                    }
                                }
                            }
                        }
                        ... on MiniSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                        ... on EndlessSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}first, after: ${'$'}after) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                                pageInfo { hasNextPage endCursor }
                            }
                        }
                    }
                 }
              }
            }
        """
        }
    }

    @McpTool(
        name = "get_series_episodes",
        description = "Get episodes for a series. " +
                "Parameters: " +
                "- seriesName (required): Name of the series to search for. " +
                "- limit (optional, default: 10): Maximum number of episodes to return. " +
                "- sortBy (optional, default: 'date_desc'): Sort order. Valid values: " +
                "'date_desc' (newest first), 'date_asc' (oldest first), " +
                "'episode_desc' (highest episode number first), 'episode_asc' (lowest episode number first)."
    )
    fun getSeriesEpisodes(
        seriesName: String,
        limit: Int? = 10,
        sortBy: String? = "date_desc",
        cursor: String? = null
    ): McpPagedResult<EpisodeNode> {
        logger.info("MCP Tool 'get_series_episodes' called with seriesName='{}', limit={}, sortBy={}, cursorPresent={}",
            seriesName, limit, sortBy, !cursor.isNullOrBlank())

        try {
            val actualLimit = limit ?: 10

            // Determine whether to include sortBy in the GraphQL query. Only include if user provided a non-blank value.
            val includeSort = !sortBy.isNullOrBlank()
            val (sortField, sortDirection) = if (includeSort) {
                parseSortBy(sortBy)
            } else {
                // placeholders (not used when includeSort==false)
                Pair("EDITORIAL_DATE", "DESC")
            }

            logger.info("MCP Tool 'get_series_episodes' executing for series='{}', limit={}, includeSort={}",
                seriesName, actualLimit, includeSort)

            val query = buildQuery(includeSort)

            var request = zdfGraphQlClient.document(query)
                .variable("query", seriesName)
                .variable("first", actualLimit)

            if (includeSort) {
                // pass sort direction as GraphQL variable (OrderByDirection enum)
                request = request.variable("sortBy", listOf(mapOf("field" to sortField, "direction" to sortDirection)))
            }

             if (!cursor.isNullOrBlank()) {
                 // Treat provided cursor as opaque GraphQL 'after' cursor
                 request = request.variable("after", cursor)
             }

            val response = request
                .retrieve("searchDocuments")
                .toEntity(SearchDocumentsResult::class.java)
                .block()

            if (response == null || response.results.isEmpty()) {
                logger.info("No results found for series '{}'", seriesName)
                return McpPagedResult(resources = emptyList(), nextCursor = null)
            }

            val item = response.results.firstOrNull()?.item

            if (item !is SeriesSmartCollection) {
                logger.info("Item is not a series collection for query '{}', got type: {}", seriesName, item?.javaClass?.simpleName)
                return McpPagedResult(resources = emptyList(), nextCursor = null)
            }

            // Prefer direct episodes if present
            val episodesConn: EpisodeConnection? = item.episodes
            val episodes = episodesConn?.nodes.orEmpty()
            val pageInfo: PageInfo? = episodesConn?.pageInfo

            // If no direct episodes, try seasons -> first season with episodes
            var finalEpisodes = episodes
            var finalPageInfo = pageInfo
            if (finalEpisodes.isEmpty()) {
                val seasonNodes = item.seasons?.nodes.orEmpty()
                if (seasonNodes.size == 1) {
                    val seasonEpisodes = seasonNodes.first().episodes
                    finalEpisodes = seasonEpisodes?.nodes.orEmpty()
                    finalPageInfo = seasonEpisodes?.pageInfo
                } else if (seasonNodes.size > 1) {
                    // Multiple seasons: aggregate episodes but we cannot reliably page across seasons -> no nextCursor
                    finalEpisodes = seasonNodes.flatMap { it.episodes?.nodes.orEmpty() }
                    finalPageInfo = null
                }
            }

            val nextCursor = if (finalPageInfo != null && finalPageInfo.hasNextPage) {
                finalPageInfo.endCursor
            } else {
                null
            }

            logger.info("Successfully retrieved {} episodes for series '{}'", finalEpisodes.size, seriesName)
            return McpPagedResult(resources = finalEpisodes, nextCursor = nextCursor)

        } catch (e: GraphQlClientException) {
            logger.error("GraphQL error executing get_series_episodes for series '{}': {}", seriesName, e.message, e)
            throw RuntimeException("Failed to get series episodes: ${e.message}", e)
        } catch (e: Exception) {
            logger.error("Error executing get_series_episodes for series '{}': {}", seriesName, e.message, e)
            throw RuntimeException("Failed to get series episodes: ${e.message}", e)
        }
    }

    internal fun parseSortBy(sortBy: String?): Pair<String, String> {
        val s = sortBy?.lowercase() ?: "date_desc"
        return when (s) {
            "date_desc" -> Pair("EDITORIAL_DATE", "DESC")
            "date_asc" -> Pair("EDITORIAL_DATE", "ASC")
            "episode_desc" -> Pair("EPISODE_NUMBER", "DESC")
            "episode_asc" -> Pair("EPISODE_NUMBER", "ASC")
            else -> {
                logger.warn("Unknown sortBy value '{}', defaulting to date_desc", sortBy)
                Pair("EDITORIAL_DATE", "DESC")
            }
        }
    }
}
