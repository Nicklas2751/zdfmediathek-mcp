package eu.wiegandt.zdfmediathekmcp.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SeriesSummaryTest {

    @Test
    fun `constructor maps ZdfSeries correctly`() {
        // given
        val zdfSeries = ZdfSeries(
            seriesUuid = "uuid-123",
            seriesTitle = "Test Series",
            seriesDescription = "A test series",
            seriesImdbId = "http://imdb.com/test",
            seriesIndexPageId = "test-series-100",
            brand = ZdfSeriesBrandReference("brand-abc")
        )

        // when
        val summary = SeriesSummary(zdfSeries)

        // then
        assertThat(summary.seriesUuid).isEqualTo("uuid-123")
        assertThat(summary.title).isEqualTo("Test Series")
        assertThat(summary.description).isEqualTo("A test series")
        assertThat(summary.brandId).isEqualTo("brand-abc")
        assertThat(summary.imdbUrl).isEqualTo("http://imdb.com/test")
        assertThat(summary.url).isEqualTo("https://www.zdf.de/test-series-100")
    }

    @Test
    fun `constructor handles null fields`() {
        // given
        val zdfSeries = ZdfSeries(
            seriesUuid = "uuid-456",
            seriesTitle = "Minimal Series",
            seriesDescription = null,
            seriesImdbId = null,
            seriesIndexPageId = null,
            brand = null
        )

        // when
        val summary = SeriesSummary(zdfSeries)

        // then
        assertThat(summary.seriesUuid).isEqualTo("uuid-456")
        assertThat(summary.title).isEqualTo("Minimal Series")
        assertThat(summary.description).isNull()
        assertThat(summary.brandId).isNull()
        assertThat(summary.imdbUrl).isNull()
        assertThat(summary.url).isNull()
    }
}

