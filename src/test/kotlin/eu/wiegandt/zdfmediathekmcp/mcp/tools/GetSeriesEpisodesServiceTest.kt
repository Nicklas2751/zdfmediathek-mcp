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
                            )
                        ),
                        seasons = null
                    )
                )
            )
        )

        doReturn(requestSpec).`when`(zdfGraphQlClient).document(anyString())
        doReturn(requestSpec).`when`(requestSpec).variable(anyString(), any())
        doReturn(retrieveSpec).`when`(requestSpec).retrieve(anyString())
        doReturn(Mono.just(apiResponse)).`when`(retrieveSpec).toEntity(SearchDocumentsResult::class.java)

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes(seriesName, 10, "date_desc")

        // then
        assertThat(result).containsExactly(EpisodeNode(
            title = "heute-show vom 1. Januar",
            editorialDate = "2024-01-01T22:30:00Z",
            sharingUrl = "https://www.zdf.de/comedy/heute-show/heute-show-vom-1-januar-2024-100.html",
            episodeInfo = EpisodeInfo(
                seasonNumber = 2024,
                episodeNumber = 1
            )
        ))
    }

    @Test
    fun `getSeriesEpisodes returns empty list when series not found`() {
        // given
        val apiResponse = SearchDocumentsResult(results = emptyList())

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes("unknown-series", 10, "date_desc")

        // then
        assertThat(result).isEmpty()
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

        `when`(zdfGraphQlClient.document(anyString())).thenReturn(requestSpec)
        `when`(requestSpec.variable(anyString(), any())).thenReturn(requestSpec)
        `when`(requestSpec.retrieve(anyString())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(SearchDocumentsResult::class.java)).thenReturn(Mono.just(apiResponse))

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes("some-video", 10, "date_desc")

        // then
        assertThat(result).isEmpty()
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
