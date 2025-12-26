package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import org.springaicommunity.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class SearchContentService(val zdfMediathekService: ZdfMediathekService) {

    @McpTool(
        name = "search_content",
        description = "Search for content in the ZDF Mediathek. The field 'webCanonical' in the response contains the URL to the content.",
    )
    fun searchContent(query: String, limit: Int = 5): ZdfSearchResponse {
        require(query.isNotBlank()) {
            "Parameter 'query' is required and must not be empty"
        }

        return zdfMediathekService.searchDocuments(query, limit)
    }

}
