package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.model.EpisodeNode
import eu.wiegandt.zdfmediathekmcp.model.SearchDocumentsResult
import eu.wiegandt.zdfmediathekmcp.model.SeriesSmartCollection
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.stereotype.Service

@Service
class GetSeriesEpisodesService(private val zdfGraphQlClient: HttpGraphQlClient) {

    private val logger = LoggerFactory.getLogger(GetSeriesEpisodesService::class.java)

    companion object {
        // Query for episodes with sorting support
        // Note: Season filtering via GraphQL API doesn't work reliably across all series types
        private fun buildQuery(sortField: String, sortDir: String) = """
            query GetSeriesEpisodes(${'$'}query: String!, ${'$'}limit: Int) {
              searchDocuments(query: ${'$'}query, first: 1) {
                 results {
                    item {
                        __typename
                        ... on DefaultNoSectionsSmartCollection {
                            title
                            episodes(first: ${'$'}limit, sortBy: [{field: $sortField, direction: $sortDir}]) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                            }
                        }
                        ... on DefaultWithSectionsSmartCollection {
                            title
                            episodes(first: ${'$'}limit, sortBy: [{field: $sortField, direction: $sortDir}]) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                            }
                        }
                        ... on SeasonSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}limit, sortBy: [{field: $sortField, direction: $sortDir}]) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                            }
                        }
                        ... on MiniSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}limit, sortBy: [{field: $sortField, direction: $sortDir}]) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                            }
                        }
                        ... on EndlessSeriesSmartCollection {
                            title
                            episodes(first: ${'$'}limit, sortBy: [{field: $sortField, direction: $sortDir}]) {
                                nodes {
                                    title
                                    editorialDate
                                    sharingUrl
                                    episodeInfo {
                                        seasonNumber
                                        episodeNumber
                                    }
                                }
                            }
                        }
                    }
                 }
              }
            }
        """
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
        sortBy: String? = "date_desc"
    ): List<EpisodeNode> {
        logger.info("MCP Tool 'get_series_episodes' called with seriesName='{}', limit={}, sortBy={}",
            seriesName, limit, sortBy)

        try {
            val actualLimit = limit ?: 10

            // Parse sortBy parameter: "date_desc", "date_asc", "episode_asc", "episode_desc"
            val (sortField, sortDirection) = parseSortBy(sortBy ?: "date_desc")

            logger.info("MCP Tool 'get_series_episodes' executing for series='{}', limit={}, sort={}:{}",
                seriesName, actualLimit, sortField, sortDirection)

            // Build query with sorting
            val query = buildQuery(sortField, sortDirection)

            val response = zdfGraphQlClient.document(query)
                .variable("query", seriesName)
                .variable("limit", actualLimit)
                .retrieve("searchDocuments")
                .toEntity(SearchDocumentsResult::class.java)
                .block()

            if (response == null || response.results.isEmpty()) {
                logger.info("No results found for series '{}'", seriesName)
                return emptyList()
            }

            val item = response.results.firstOrNull()?.item

            if (item !is SeriesSmartCollection) {
                logger.info("Item is not a series collection for query '{}', got type: {}", seriesName, item?.javaClass?.simpleName)
                return emptyList()
            }

            // Get episodes directly from series
            val episodes = item.episodes?.nodes.orEmpty()

            logger.info("Successfully retrieved {} episodes for series '{}'", episodes.size, seriesName)
            return episodes

        } catch (e: org.springframework.graphql.client.GraphQlClientException) {
            logger.error("GraphQL error executing get_series_episodes for series '{}': {}", seriesName, e.message, e)
            throw RuntimeException("Failed to get series episodes: ${e.message}", e)
        } catch (e: Exception) {
            logger.error("Error executing get_series_episodes for series '{}': {}", seriesName, e.message, e)
            throw RuntimeException("Failed to get series episodes: ${e.message}", e)
        }
    }

    private fun parseSortBy(sortBy: String): Pair<String, String> {
        return when (sortBy.lowercase()) {
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
