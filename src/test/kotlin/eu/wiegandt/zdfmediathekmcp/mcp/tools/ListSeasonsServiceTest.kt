package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.springframework.web.reactive.function.client.WebClientResponseException

class ListSeasonsServiceTest {
    private val zdfMediathekClient = Mockito.mock(ZdfMediathekClient::class.java)
    private val listSeasonsService = ListSeasonsService(zdfMediathekClient)

    @Test
    fun `listSeasons returns expected seasons`() {
        // given
        val apiResponse = ZdfSeasonResponse(
            listOf(
                ZdfSeason(
                    seasonUuid = "season-uuid-1",
                    seasonNumber = 1,
                    seasonTitle = "Staffel 1",
                    brand = ZdfSeriesBrandReference("brand-uuid-1"),
                    series = ZdfSeries(
                        seriesUuid = "series-uuid-1",
                        seriesTitle = "Series 1",
                        seriesDescription = "Description 1",
                        seriesImdbId = "http://imdb.com/series1",
                        seriesIndexPageId = "series-1",
                        brand = ZdfSeriesBrandReference("brand-uuid-1")
                    )
                ),
                ZdfSeason(
                    seasonUuid = "season-uuid-2",
                    seasonNumber = 2,
                    seasonTitle = "Staffel 2",
                    brand = null,
                    series = null
                )
            )
        )
        val expected = listOf(
            SeasonSummary(
                seasonUuid = "season-uuid-1",
                seasonNumber = 1,
                title = "Staffel 1",
                brandId = "brand-uuid-1",
                series = SeriesSummary(
                    seriesUuid = "series-uuid-1",
                    title = "Series 1",
                    description = "Description 1",
                    brandId = "brand-uuid-1",
                    imdbUrl = "http://imdb.com/series1",
                    url = "https://www.zdf.de/series-1"
                )
            ),
            SeasonSummary(
                seasonUuid = "season-uuid-2",
                seasonNumber = 2,
                title = "Staffel 2",
                brandId = null,
                series = null
            )
        )
        doReturn(apiResponse).`when`(zdfMediathekClient).listSeasons(4)

        // when
        val result = listSeasonsService.listSeasons()

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun `listSeasons with empty result returns empty list`() {
        // given
        doReturn(ZdfSeasonResponse()).`when`(zdfMediathekClient).listSeasons(4)

        // when
        val result = listSeasonsService.listSeasons()

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `listSeasons passes limit parameter`() {
        // given
        doReturn(ZdfSeasonResponse()).`when`(zdfMediathekClient).listSeasons(5)

        // when
        listSeasonsService.listSeasons(5)

        // then
        Mockito.verify(zdfMediathekClient).listSeasons(5)
    }

    @Test
    fun `listSeasons throws on API error`() {
        // given
        doThrow(
            WebClientResponseException(
                500,
                "Server Error",
                null,
                null,
                null
            )
        ).`when`(zdfMediathekClient).listSeasons(4)

        // when/then
        assertThatThrownBy { listSeasonsService.listSeasons() }
            .isInstanceOf(RuntimeException::class.java)
    }
}

