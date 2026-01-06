package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfSeriesResponse(
    @field:JsonProperty("http://zdf.de/rels/cmdm/series")
    val series: List<ZdfSeries> = emptyList()
)

