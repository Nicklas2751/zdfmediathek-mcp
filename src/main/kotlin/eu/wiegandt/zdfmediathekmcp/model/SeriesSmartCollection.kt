package eu.wiegandt.zdfmediathekmcp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeriesSmartCollection(
    override val title: String,
    // Direct episodes (Case A)
    val episodes: EpisodeConnection?,
    // Seasons with episodes (Case B)
    val seasons: SeasonConnection?
) : SearchResultItem
