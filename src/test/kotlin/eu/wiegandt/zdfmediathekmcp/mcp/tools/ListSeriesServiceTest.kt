package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.SeriesSummary
import eu.wiegandt.zdfmediathekmcp.model.ZdfSeries
import eu.wiegandt.zdfmediathekmcp.model.ZdfSeriesBrandReference
import eu.wiegandt.zdfmediathekmcp.model.ZdfSeriesResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.springframework.web.reactive.function.client.WebClientResponseException

class ListSeriesServiceTest {
    private val zdfMediathekClient = Mockito.mock(ZdfMediathekClient::class.java)
    private val listSeriesService = ListSeriesService(zdfMediathekClient)

    @Test
    fun `listSeries returns expected series`() {
        // given
        val apiResponse = ZdfSeriesResponse(
            listOf(
                ZdfSeries(
                    seriesUuid = "uuid1",
                    seriesTitle = "Series 1",
                    seriesDescription = "Desc 1",
                    seriesImdbId = "http://imdb.com/1",
                    seriesIndexPageId = "page-1",
                    brand = ZdfSeriesBrandReference("brand1")
                ),
                ZdfSeries(
                    seriesUuid = "uuid2",
                    seriesTitle = "Series 2",
                    seriesDescription = null,
                    seriesImdbId = null,
                    seriesIndexPageId = "page-2",
                    brand = null
                )
            )
        )
        val expected = listOf(
            SeriesSummary(
                seriesUuid = "uuid1",
                title = "Series 1",
                description = "Desc 1",
                brandId = "brand1",
                imdbUrl = "http://imdb.com/1",
                url = "https://www.zdf.de/page-1"
            ),
            SeriesSummary(
                seriesUuid = "uuid2",
                title = "Series 2",
                description = null,
                brandId = null,
                imdbUrl = null,
                url = "https://www.zdf.de/page-2"
            )
        )
        doReturn(apiResponse).`when`(zdfMediathekClient).listSeries(4)

        // when
        val result = listSeriesService.listSeries()

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun `listSeries with empty result returns empty list`() {
        // given
        doReturn(ZdfSeriesResponse()).`when`(zdfMediathekClient).listSeries(4)

        // when
        val result = listSeriesService.listSeries()

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `listSeries passes limit parameter`() {
        // given
        doReturn(ZdfSeriesResponse()).`when`(zdfMediathekClient).listSeries(5)

        // when
        listSeriesService.listSeries(5)

        // then
        Mockito.verify(zdfMediathekClient).listSeries(5)
    }

    @Test
    fun `listSeries throws on API error`() {
        // given
        doThrow(
            WebClientResponseException(
                500,
                "Server Error",
                null,
                null,
                null
            )
        ).`when`(zdfMediathekClient).listSeries(4)

        // when/then
        assertThatThrownBy { listSeriesService.listSeries() }
            .isInstanceOf(RuntimeException::class.java)
    }
}

