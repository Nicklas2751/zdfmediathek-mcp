package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZdfSearchResult(
    val score: Double = 0.0,
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val resultType: String = "default",
    @field:JsonProperty("http://zdf.de/rels/target")
    val target: ZdfDocument? = null
)
