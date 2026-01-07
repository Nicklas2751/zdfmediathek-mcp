package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfSeasonResponse(
    @field:JsonProperty("http://zdf.de/rels/cmdm/seasons")
    val seasons: List<ZdfSeason> = emptyList()
)

