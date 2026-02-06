package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.graphql.client.GraphQlClient
import org.springframework.graphql.client.GraphQlClientException
import org.springframework.graphql.client.HttpGraphQlClient
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
class GetSeriesEpisodesServiceTest {

    @Mock
    private lateinit var zdfGraphQlClient: HttpGraphQlClient

    @Mock
    private lateinit var requestSpec: GraphQlClient.RequestSpec

    @Mock
    private lateinit var retrieveSpec: GraphQlClient.RetrieveSpec

    @InjectMocks
    private lateinit var getSeriesEpisodesService: GetSeriesEpisodesService

    @Test
    fun `getSeriesEpisodes returns episodes when series found`() {
        // given
        val seriesName = "heute-show"
        val apiResponse = SearchDocumentsResult(
            results = listOf(
                SearchResultItemWrapper(
                    item = SeriesSmartCollection(
                        title = "heute-show",
                        episodes = EpisodeConnection(
                            nodes = listOf(
                                EpisodeNode(
                                    title = "heute-show vom 1. Januar",
                                    editorialDate = "2024-01-01T22:30:00Z",
                                    sharingUrl = "https://www.zdf.de/comedy/heute-show/heute-show-vom-1-januar-2024-100.html",
                                    episodeInfo = EpisodeInfo(
                                        seasonNumber = 2024,
                                        episodeNumber = 1
                                    )
                                )
                            ),
                            pageInfo = null
                        ),
                        seasons = null
                    )
                )
            )
        )
        val expected = McpPagedResult(
            resources = listOf(
                EpisodeNode(
                    title = "heute-show vom 1. Januar",
                    editorialDate = "2024-01-01T22:30:00Z",
                    sharingUrl = "https://www.zdf.de/comedy/heute-show/heute-show-vom-1-januar-2024-100.html",
                    episodeInfo = EpisodeInfo(
                        seasonNumber = 2024,
                        episodeNumber = 1
                    )
                )
            ),
            nextCursor = null
        )

        doReturn(requestSpec).`when`(zdfGraphQlClient).document(anyString())
        doReturn(requestSpec).`when`(requestSpec).variable(anyString(), any())
        doReturn(retrieveSpec).`when`(requestSpec).retrieve(anyString())
        doReturn(Mono.just(apiResponse)).`when`(retrieveSpec).toEntity(SearchDocumentsResult::class.java)

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes(seriesName, 10, "date_desc")

        // then
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `getSeriesEpisodes returns empty list when series not found`() {
        // given
        val apiResponse = SearchDocumentsResult(results = emptyList())
        val expected = McpPagedResult(
            resources = emptyList<EpisodeNode>(),
            nextCursor = null
        )

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes("unknown-series", 10, "date_desc")

        // then
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `getSeriesEpisodes returns empty list when item is not a series`() {
        // given
        val apiResponse = SearchDocumentsResult(
            results = listOf(
                SearchResultItemWrapper(
                    item = VideoItem(
                        title = "Some Video",
                        episodeInfo = null,
                        editorialDate = null,
                        sharingUrl = null
                    )
                )
            )
        )
        val expected = McpPagedResult(
            resources = emptyList<EpisodeNode>(),
            nextCursor = null
        )

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes("some-video", 10, "date_desc")

        // then
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `getSeriesEpisodes handles single season correctly`() {
        // given
        val seriesName = "Series With One Season"
        // Setup a SeriesSmartCollection with no direct episodes but 1 season
        val seasonEpisodes = EpisodeConnection(
            nodes = listOf(EpisodeNode("S1E1", "2024-01-01T00:00:00Z", "url1", EpisodeInfo(1, 1))),
            pageInfo = PageInfo(hasNextPage = true, endCursor = "cursor-next")
        )
        val seasons = SeasonConnection(
            nodes = listOf(SeasonNode(1, seasonEpisodes))
        )
        val apiResponse = SearchDocumentsResult(
            results = listOf(
                SearchResultItemWrapper(
                    item = SeriesSmartCollection("Title", null, seasons)
                )
            )
        )
        val expected = McpPagedResult(
            resources = listOf(EpisodeNode("S1E1", "2024-01-01T00:00:00Z", "url1", EpisodeInfo(1, 1))),
            nextCursor = "cursor-next"
        )

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes(seriesName)

        // then
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `getSeriesEpisodes handles multiple seasons by flattening and logic should gracefully ignore pagination`() {
        // given
        val seriesName = "Series With Many Seasons"
        val ep1 = EpisodeNode("S1E1", "2024-01-01T00:00:00Z", "url1", EpisodeInfo(1, 1))
        val ep2 = EpisodeNode("S2E1", "2024-02-01T00:00:00Z", "url2", EpisodeInfo(2, 1))

        val seasons = SeasonConnection(
            nodes = listOf(
                SeasonNode(1, EpisodeConnection(listOf(ep1), PageInfo(false, null))),
                SeasonNode(2, EpisodeConnection(listOf(ep2), PageInfo(true, "ignored-cursor")))
            )
        )
        val apiResponse = SearchDocumentsResult(
            results = listOf(
                SearchResultItemWrapper(
                    item = SeriesSmartCollection("Title", null, seasons)
                )
            )
        )
        val expected = McpPagedResult(
            resources = listOf(ep1, ep2),
            nextCursor = null
        )

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes(seriesName)

        // then
        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }

    @Test
    fun `getSeriesEpisodes passes sortBy and cursor variables correctly`() {
        // given
        val cursor = "base64cursor"
        val apiResponse = SearchDocumentsResult(results = emptyList())

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        getSeriesEpisodesService.getSeriesEpisodes("Series", 5, "date_asc", cursor)

        // then
        // Verify variables were passed
        verify(requestSpec).variable("query", "Series")
        verify(requestSpec).variable("first", 5)
        verify(requestSpec).variable("sortBy", listOf(mapOf("field" to "EDITORIAL_DATE", "direction" to "ASC")))
        verify(requestSpec).variable("after", cursor)
    }

    @Test
    fun `parseSortBy date_desc returns editorial_date_desc`() {
        val p = getSeriesEpisodesService.parseSortBy("date_desc")
        assertThat(p).isEqualTo(Pair("EDITORIAL_DATE", "DESC"))
    }

    @Test
    fun `parseSortBy date_asc returns editorial_date_asc`() {
        val p = getSeriesEpisodesService.parseSortBy("date_asc")
        assertThat(p).isEqualTo(Pair("EDITORIAL_DATE", "ASC"))
    }

    @Test
    fun `parseSortBy episode_desc returns episode_number_desc`() {
        val p = getSeriesEpisodesService.parseSortBy("episode_desc")
        assertThat(p).isEqualTo(Pair("EPISODE_NUMBER", "DESC"))
    }

    @Test
    fun `parseSortBy episode_asc returns episode_number_asc`() {
        val p = getSeriesEpisodesService.parseSortBy("episode_asc")
        assertThat(p).isEqualTo(Pair("EPISODE_NUMBER", "ASC"))
    }

    @Test
    fun `parseSortBy unknown defaults to date_desc`() {
        val p = getSeriesEpisodesService.parseSortBy("something_else")
        assertThat(p).isEqualTo(Pair("EDITORIAL_DATE", "DESC"))
    }

    @Test
    fun `getSeriesEpisodes wraps GraphQlClientException into RuntimeException`() {
        // given
        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        val gqlEx = mock(GraphQlClientException::class.java)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.error(gqlEx))

        // when / then
        val ex = assertThrows(RuntimeException::class.java) {
            getSeriesEpisodesService.getSeriesEpisodes("some-series", 5, "date_desc")
        }

        assertThat(ex.message).contains("Failed to get series episodes")
        assertThat(ex.cause).isInstanceOf(GraphQlClientException::class.java)
    }

    @Test
    fun `getSeriesEpisodes wraps generic Exception into RuntimeException`() {
        // given: make the client throw a non-GraphQlClientException
        doThrow(IllegalStateException("connection lost")).`when`(zdfGraphQlClient).document(anyString())

        // when / then
        val ex = assertThrows(RuntimeException::class.java) {
            getSeriesEpisodesService.getSeriesEpisodes("some-series", 5, "date_desc")
        }

        assertThat(ex.message).contains("Failed to get series episodes")
        assertThat(ex.cause).isInstanceOf(IllegalStateException::class.java)
    }
}
