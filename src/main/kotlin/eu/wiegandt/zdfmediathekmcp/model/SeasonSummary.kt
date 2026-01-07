package eu.wiegandt.zdfmediathekmcp.model

data class SeasonSummary(
    val seasonUuid: String,   // seasonUuid (essential for broadcast lookups)
    val seasonNumber: Int?,   // seasonNumber
    val title: String,        // seasonTitle
    val series: SeriesSummary?, // Embedded series details (see list_series)
    val brandId: String?      // references brandUuid
)

