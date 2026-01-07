package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfSeason(
    val seasonUuid: String = "",
    val seasonNumber: Int? = null,
    val seasonTitle: String = "",
    @field:JsonProperty("http://zdf.de/rels/cmdm/series")
    val series: ZdfSeries? = null,
    @field:JsonProperty("http://zdf.de/rels/cmdm/brand")
    val brand: ZdfSeriesBrandReference? = null
)

