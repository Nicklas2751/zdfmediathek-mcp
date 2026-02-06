package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.BrandApiResponse
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class ListBrandsService(private val zdfMediathekClient: ZdfMediathekClient) {
    private val logger = LoggerFactory.getLogger(ListBrandsService::class.java)

    /**
     * MCP Tool: List all TV brands/series in the ZDF Mediathek.
     * @param limit Maximale Anzahl der Ergebnisse (optional, default: 10)
     * @param cursor Optionaler MCP-Paging-Cursor (Base64-encoded JSON {"page":X,"limit":Y})
     * @return McpPagedResult with resources and optional nextCursor
     */
    @McpTool(
        name = "list_brands",
        description = """List all TV brands/series in the ZDF Mediathek. 
                Parameters: limit (optional, default: 10), cursor (optional, MCP paging cursor).
                Returns a list of brands with uuid, brandName, and brandDescription. 
                """
    )
    fun listBrands(limit: Int? = 10, cursor: String? = null): McpPagedResult<BrandSummary> {
        var actualLimit = limit ?: 10
        var page = 1

        if (!cursor.isNullOrBlank()) {
            try {
                val payload = McpPaginationPayloadHandler.decode(cursor)
                page = payload.page
                payload.limit?.let { actualLimit = it }
            } catch (e: IllegalArgumentException) {
                logger.warn("Invalid pagination cursor provided for list_brands: {}", e.message)
                throw e
            }
        }

        logger.info("MCP Tool 'list_brands' called with limit={} page={}", actualLimit, page)
        try {
            val result: BrandApiResponse = zdfMediathekClient.listBrands(actualLimit, page)
            logger.info("Successfully retrieved {} brands", result.brands.size)

            val resources = result.brands

            // If API returns an explicit next-archive link, we don't need to parse it — just advance page by 1
            val nextCursor = if (!result.nextArchive.isNullOrBlank()) {
                McpPaginationPayloadHandler.encode(page + 1, actualLimit)
            } else if (resources.size >= actualLimit) {
                // Fallback heuristic: if we got a full page, assume there may be a next page
                McpPaginationPayloadHandler.encode(page + 1, actualLimit)
            } else {
                null
            }

            return McpPagedResult(resources = resources, nextCursor = nextCursor)
        } catch (e: Exception) {
            logger.error("Error executing list_brands: {}", e.message, e)
            throw RuntimeException("Failed to list brands: ${e.message}", e)
        }
    }
}