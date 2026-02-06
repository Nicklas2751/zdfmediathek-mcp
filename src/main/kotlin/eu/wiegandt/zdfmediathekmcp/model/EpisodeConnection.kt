package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EpisodeConnection(
    val nodes: List<EpisodeNode>,
    val pageInfo: PageInfo? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EpisodeNode(
    val title: String,
    val editorialDate: String?,
    val sharingUrl: String?,
    val episodeInfo: EpisodeInfo?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EpisodeInfo(
    val seasonNumber: Int?,
    val episodeNumber: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageInfo(
    val hasNextPage: Boolean,
    val endCursor: String?
)
