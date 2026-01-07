package eu.wiegandt.zdfmediathekmcp.model

data class SeriesSummary(
    val seriesUuid: String,   // seriesUuid (essential for broadcast lookups)
    val title: String,        // seriesTitle
    val description: String?, // seriesDescription
    val brandId: String?,     // references brandUuid
    val imdbUrl: String?,     // seriesImdbId (nullable, not always present)
    val url: String?          // derived from https://www.zdf.de/{seriesIndexPageId}
) {
    constructor(zdfSeries: ZdfSeries) : this(
        seriesUuid = zdfSeries.seriesUuid,
        title = zdfSeries.seriesTitle,
        description = zdfSeries.seriesDescription,
        brandId = zdfSeries.brand?.brandUuid,
        imdbUrl = zdfSeries.seriesImdbId,
        url = zdfSeries.seriesIndexPageId?.let { "https://www.zdf.de/$it" }
    )
}
