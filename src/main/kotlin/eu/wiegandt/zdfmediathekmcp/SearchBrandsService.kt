package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class SearchBrandsService(private val zdfMediathekService: ZdfMediathekService) {
    private val logger = LoggerFactory.getLogger(SearchBrandsService::class.java)

    /**
     * MCP Tool: List all TV brands/series in the ZDF Mediathek.
     * @param limit Maximale Anzahl der Ergebnisse (default: 10)
     * @return Liste von BrandSummary-Objekten
     */
    @McpTool(
        name = "list_brands",
        description = "List all TV brands/series in the ZDF Mediathek. Returns a list of brands with uuid, brandName, and brandDescription. " +
                "Parameter: limit (optional, default: 10)."
    )
    fun listBrands(limit: Int = 10): List<BrandSummary> {
        logger.info("MCP Tool 'list_brands' called with limit={}", limit)
        try {
            val result = zdfMediathekService.listBrands(limit).brands
            logger.info("Successfully retrieved {} brands", result.size)
            return result
        } catch (e: Exception) {
            logger.error("Error executing list_brands: {}", e.message, e)
            throw RuntimeException("Failed to list brands: ${e.message}", e)
        }
    }
}
