package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfSeries(
    val seriesUuid: String = "",
    val seriesTitle: String = "",
    val seriesDescription: String? = null,
    val seriesImdbId: String? = null,
    val seriesIndexPageId: String? = null,
    @field:JsonProperty("http://zdf.de/rels/cmdm/brand")
    val brand: ZdfSeriesBrandReference? = null
)

