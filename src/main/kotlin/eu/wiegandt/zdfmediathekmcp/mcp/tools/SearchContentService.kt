package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResult
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
            - cursor: Optional MCP pagination cursor (Base64-encoded JSON {"page":X,"limit":Y})
            The field 'webCanonical' in the response contains the URL to the content.
            Returns a paged result containing search results.
        """,
    )
    fun searchContent(query: String, limit: Int? = 5, cursor: String? = null): McpPagedResult<ZdfSearchResult> {
        val actualLimit = limit ?: 5
        var page = 1

        logger.info("MCP Tool 'search_content' called with query='{}', limit={}, cursorPresent={}", query, actualLimit, !cursor.isNullOrBlank())

        try {
            // Validate parameters
            require(query.isNotBlank()) {
                "Parameter 'query' is required and must not be empty"
            }

            if (!cursor.isNullOrBlank()) {
                try {
                    val payload = McpPaginationPayloadHandler.decode(cursor)
                    page = payload.page
                    payload.limit?.let { l ->
                        // override limit if encoded in cursor
                        // keep actualLimit variable
                    }
                } catch (e: IllegalArgumentException) {
                    logger.warn("Invalid pagination cursor provided for search_content: {}", e.message)
                    throw e
                }
            }

            logger.debug("Calling ZDF API to search documents (limit={}, page={})", actualLimit, page)
            val response: ZdfSearchResponse = zdfMediathekClient.searchDocuments(query, actualLimit, page)

            logger.info(
                "Successfully retrieved {} search results for query '{}'",
                response.results.size, query
            )

            val nextCursor = if (response.results.size >= actualLimit) {
                McpPaginationPayloadHandler.encode(page + 1, actualLimit)
            } else {
                null
            }

            return McpPagedResult(resources = response.results.map { it }, nextCursor = nextCursor)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid parameter for search_content: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("Error executing search_content for query '{}': {}", query, e.message, e)
            throw RuntimeException("Failed to search ZDF Mediathek: ${e.message}", e)
        }
    }

}