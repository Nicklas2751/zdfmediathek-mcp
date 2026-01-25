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
import java.util.*

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
        val expectedResources = listOf(
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
        doReturn(apiResponse).`when`(zdfMediathekClient).listSeries(4, 1)

        // when
        val result: McpPagedResult<SeriesSummary> = listSeriesService.listSeries()

        // then
        assertThat(result.resources).usingRecursiveComparison().isEqualTo(expectedResources)
        // Since apiResponse has 2 items and limit is 4, nextCursor should be null
        assertThat(result.nextCursor).isNull()
    }

    @Test
    fun `listSeries with full page returns nextCursor`() {
        // given: response size == limit -> nextCursor expected
        val apiResponse = ZdfSeriesResponse(List(4) { idx ->
            ZdfSeries(seriesUuid = "id$idx", seriesTitle = "t$idx", seriesDescription = null, seriesImdbId = null, seriesIndexPageId = "p$idx", brand = null)
        })
        doReturn(apiResponse).`when`(zdfMediathekClient).listSeries(4, 1)

        // when
        val result: McpPagedResult<SeriesSummary> = listSeriesService.listSeries()

        // then
        assertThat(result.resources).hasSize(4)
        assertThat(result.nextCursor).isNotNull
    }

    @Test
    fun `listSeries with empty result returns empty list`() {
        // given
        doReturn(ZdfSeriesResponse()).`when`(zdfMediathekClient).listSeries(4, 1)

        // when
        val result = listSeriesService.listSeries()

        // then
        assertThat(result.resources).isEmpty()
    }

    @Test
    fun `listSeries passes limit parameter`() {
        // given
        doReturn(ZdfSeriesResponse()).`when`(zdfMediathekClient).listSeries(5, 1)

        // when
        listSeriesService.listSeries(5)

        // then
        Mockito.verify(zdfMediathekClient).listSeries(5, 1)
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
        ).`when`(zdfMediathekClient).listSeries(4, 1)

        // when/then
        assertThatThrownBy { listSeriesService.listSeries() }
            .isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `listSeries with cursor decodes and calls client with correct page`() {
        // given: cursor encodes page=2 and limit=3
        val payload = "{\"page\":2,\"limit\":3}"
        val cursor = Base64.getEncoder().encodeToString(payload.toByteArray())
        val apiResponse = ZdfSeriesResponse()
        doReturn(apiResponse).`when`(zdfMediathekClient).listSeries(3, 2)

        // when
        listSeriesService.listSeries(null, cursor)

        // then
        Mockito.verify(zdfMediathekClient).listSeries(3, 2)
    }

    @Test
    fun `listSeries with invalid cursor throws IllegalArgumentException`() {
        // given
        val invalidCursor = "not-base64"

        // when/then
        assertThatThrownBy { listSeriesService.listSeries(null, invalidCursor) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid cursor")
    }
}
