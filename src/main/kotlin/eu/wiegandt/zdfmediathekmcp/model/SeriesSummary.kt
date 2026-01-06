package eu.wiegandt.zdfmediathekmcp.model

data class SeriesSummary(
    val seriesUuid: String,   // seriesUuid (essential for broadcast lookups)
    val title: String,        // seriesTitle
    val description: String?, // seriesDescription
    val brandId: String?,     // references brandUuid
    val imdbUrl: String?,     // seriesImdbId (nullable, not always present)
    val url: String?          // derived from https://www.zdf.de/{seriesIndexPageId}
)

