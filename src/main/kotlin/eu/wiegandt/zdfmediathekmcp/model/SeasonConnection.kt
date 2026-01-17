package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeasonConnection(
    val nodes: List<SeasonNode>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeasonNode(
    val seasonNumber: Int?,
    val episodes: EpisodeConnection?
)
