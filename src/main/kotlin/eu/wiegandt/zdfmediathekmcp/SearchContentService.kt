package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class SearchContentService(val zdfMediathekService: ZdfMediathekService) {

    private val logger = LoggerFactory.getLogger(SearchContentService::class.java)

    @McpTool(
        name = "search_content",
        description = "Search for content in the ZDF Mediathek. The field 'webCanonical' in the response contains the URL to the content.",
    )
    fun searchContent(query: String, limit: Int = 5): ZdfSearchResponse {
        logger.info("MCP Tool 'search_content' called with query='{}', limit={}", query, limit)

        try {
            // Validate parameters
            require(query.isNotBlank()) {
                "Parameter 'query' is required and must not be empty"
            }

            logger.debug("Calling ZDF API to search documents")
            val response = zdfMediathekService.searchDocuments(query, limit)

            logger.info(
                "Successfully retrieved {} search results for query '{}'",
                response.results.size, query
            )
            logger.debug(
                "Search response: totalResultsCount={}, results={}",
                response.totalResultsCount, response.results.size
            )

            return response
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid parameter for search_content: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("Error executing search_content for query '{}': {}", query, e.message, e)
            throw RuntimeException("Failed to search ZDF Mediathek: ${e.message}", e)
        }
    }

}
