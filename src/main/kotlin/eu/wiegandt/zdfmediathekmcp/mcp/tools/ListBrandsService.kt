package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import org.slf4j.LoggerFactory
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class ListBrandsService(private val zdfMediathekClient: ZdfMediathekClient) {
    private val logger = LoggerFactory.getLogger(ListBrandsService::class.java)

    /**
     * MCP Tool: List all TV brands/series in the ZDF Mediathek.
     * @param limit Maximale Anzahl der Ergebnisse (default: 10)
     * @return Liste von BrandSummary-Objekten
     */
    @McpTool(
        name = "list_brands",
        description = """List all TV brands/series in the ZDF Mediathek. 
                Parameter: limit (optional, default: 10).
                Returns a list of brands with uuid, brandName, and brandDescription. 
                """
    )
    fun listBrands(limit: Int = 10): List<BrandSummary> {
        logger.info("MCP Tool 'list_brands' called with limit={}", limit)
        try {
            val result = zdfMediathekClient.listBrands(limit).brands
            logger.info("Successfully retrieved {} brands", result.size)
            return result
        } catch (e: Exception) {
            logger.error("Error executing list_brands: {}", e.message, e)
            throw RuntimeException("Failed to list brands: ${e.message}", e)
        }
    }
}