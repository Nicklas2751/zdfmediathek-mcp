package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BrandApiResponse(
    @field:JsonProperty("http://zdf.de/rels/cmdm/brands")
    val brands: List<BrandSummary> = emptyList()
)
