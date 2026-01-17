package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class VideoItem(
    override val title: String,
    val episodeInfo: EpisodeInfo?,
    val editorialDate: String?,
    val sharingUrl: String?
) : SearchResultItem
