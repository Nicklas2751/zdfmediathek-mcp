package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class SearchContentService(val zdfMediathekClient: ZdfMediathekClient) {

    private val logger = LoggerFactory.getLogger(SearchContentService::class.java)

    @McpTool(
        name = "search_content",
        description = """Search for content in the ZDF Mediathek.
            Parameters: 
            - query: The search query string.
            - limit: Maximum number of broadcasts to return (default: 10). 
            The field 'webCanonical' in the response contains the URL to the content.
            Returns a list of documents.
        """,
    )
    fun searchContent(query: String, limit: Int? = 5): ZdfSearchResponse {
        val actualLimit = limit ?: 5
        logger.info("MCP Tool 'search_content' called with query='{}', limit={}", query, actualLimit)

        try {
            // Validate parameters
            require(query.isNotBlank()) {
                "Parameter 'query' is required and must not be empty"
            }

            logger.debug("Calling ZDF API to search documents")
            val response = zdfMediathekClient.searchDocuments(query, actualLimit)

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