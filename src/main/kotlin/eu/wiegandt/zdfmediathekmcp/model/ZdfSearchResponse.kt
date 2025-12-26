package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfSearchResponse(
    val totalResultsCount: Int = 0,
    val next: String? = null,
    @field:JsonProperty("http://zdf.de/rels/search/results")
    val results: List<ZdfSearchResult> = emptyList()
)
