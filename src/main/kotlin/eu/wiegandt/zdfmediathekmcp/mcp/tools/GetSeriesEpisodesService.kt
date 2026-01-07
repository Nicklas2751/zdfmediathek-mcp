package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.model.EpisodeNode
import eu.wiegandt.zdfmediathekmcp.model.SeriesSmartCollection
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.graphql.client.HttpGraphQlClient
import org.springframework.stereotype.Service

@Service
class GetSeriesEpisodesService(private val zdfGraphQlClient: HttpGraphQlClient) {

    private val logger = LoggerFactory.getLogger(GetSeriesEpisodesService::class.java)

    companion object {
        // Minimal test query - using smartCollectionByCanonical which should always work
            query TestQuery($canonical: String!) {
            query GetSeriesEpisodes($query: String!, $limit: Int) {
              searchDocuments(query: $query, first: 1) {
                 results {
                    item {
                        ... on ISeriesSmartCollection {
                            title
                            episodes(first: $limit) {
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
        """
    }

    @McpTool(
        name = "get_series_episodes",
        description = "Get episodes for a series. " +
                "Parameters: seriesName (required), limit (optional, default: 10)."
    )
    fun getSeriesEpisodes(
        seriesName: String,
        seasonNumber: Int? = null,
        limit: Int? = 10,
        sortBy: String? = "date_desc"
    ): List<EpisodeNode> {
        logger.info("MCP Tool 'get_series_episodes' called with seriesName='{}', limit={}", seriesName, limit)

        // TODO: Season filtering and sorting will be implemented once basic query works
        if (seasonNumber != null) {
            logger.warn("Season filtering not yet implemented, ignoring seasonNumber parameter")
        }
        if (sortBy != "date_desc") {
                .variable("query", seriesName)
                .variable("limit", actualLimit)
                .retrieve("searchDocuments")
                .toEntity(SearchDocumentsResult::class.java)
        try {
            val item = response.results.firstOrNull()?.item

            if (item !is SeriesSmartCollection) {
                logger.info("Item is not a series collection for query '{}'", seriesName)
                return emptyList()
            }

            val episodes = item.episodes?.nodes.orEmpty()

            logger.info("Successfully retrieved {} episodes for series '{}'", episodes.size, seriesName)
            return episodes

            logger.info("GraphQL Response received: {}", response)

            if (response == null) {
                logger.info("No result found for canonical '{}'", canonical)
                return emptyList()
            }

            // For now, return empty list as this is just a test query
            // TODO: Implement proper episode fetching once we know the correct GraphQL schema
            logger.warn("Minimal test query successful, but episode fetching not yet implemented")
            logger.warn("Found collection: id={}, title={}", response.id, response.title)
            return emptyList()

        } catch (e: Exception) {
            logger.error("Error executing get_series_episodes for series '{}': {}", seriesName, e.message, e)
            throw RuntimeException("Failed to get series episodes: ${e.message}", e)
        }
    }
}
