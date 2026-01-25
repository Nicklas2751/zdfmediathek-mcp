package eu.wiegandt.zdfmediathekmcp.mcp.tools

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.BrandApiResponse
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import eu.wiegandt.zdfmediathekmcp.model.McpPagedResult
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service
import java.util.*

@Service
class ListBrandsService(private val zdfMediathekClient: ZdfMediathekClient) {
    private val logger = LoggerFactory.getLogger(ListBrandsService::class.java)
    private val objectMapper = jacksonObjectMapper()

    data class CursorPayload(val page: Int = 1, val limit: Int? = null)

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
                val decoded = String(Base64.getDecoder().decode(cursor))
                val payload = objectMapper.readValue<CursorPayload>(decoded)
                page = payload.page
                payload.limit?.let { actualLimit = it }
            } catch (e: Exception) {
                logger.warn("Invalid pagination cursor provided for list_brands: {}", e.message)
                throw IllegalArgumentException("Invalid cursor")
            }
        }

        logger.info("MCP Tool 'list_brands' called with limit={} page={}", actualLimit, page)
        try {
            val result: BrandApiResponse = zdfMediathekClient.listBrands(actualLimit, page)
            logger.info("Successfully retrieved {} brands", result.brands.size)

            val resources = result.brands

            // The /cmdm/brands endpoint does not reliably support retrieving further pages
            // (no total/hasNext information). Per project plan, do not set a nextCursor for this endpoint.
            return McpPagedResult(resources = resources, nextCursor = null)
        } catch (e: Exception) {
            logger.error("Error executing list_brands: {}", e.message, e)
            throw RuntimeException("Failed to list brands: ${e.message}", e)
        }
    }
}