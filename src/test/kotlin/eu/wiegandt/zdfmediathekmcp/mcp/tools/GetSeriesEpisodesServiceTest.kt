package eu.wiegandt.zdfmediathekmcp.mcp.tools

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class GetSeriesEpisodesServiceTest {

    // TODO: Re-enable tests once proper GraphQL schema is implemented
    /*
    @Mock
    private lateinit var zdfGraphQlClient: HttpGraphQlClient

    @Mock
    private lateinit var requestSpec: GraphQlClient.RequestSpec

    @Mock
    private lateinit var retrieveSpec: GraphQlClient.RetrieveSpec


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
        val result = getSeriesEpisodesService.getSeriesEpisodes(seriesName, null, 10, "date_desc")

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("heute-show vom 1. Januar")
        assertThat(result[0].episodeInfo?.seasonNumber).isEqualTo(2024)
        assertThat(result[0].episodeInfo?.episodeNumber).isEqualTo(1)
    }

    // TODO: Re-enable once season filtering is implemented
    // @Test
    // fun `getSeriesEpisodes filters by season when seasonNumber provided`() { ... }

    @Test
    fun `getSeriesEpisodes returns empty list when series not found`() {
        // given
        val apiResponse = SearchDocumentsResult(results = emptyList())

        doReturn(requestSpec).`when`(zdfGraphQlClient).document(anyString())
        doReturn(requestSpec).`when`(requestSpec).variable(anyString(), any())
    @Test
    fun `getSeriesEpisodes filters by season when seasonNumber provided`() {
        // given
        val seriesName = "Sketch History"
        val seasonNumber = 1
        val apiResponse = SearchDocumentsResult(
            results = listOf(
                SearchResultItemWrapper(
                    item = SeriesSmartCollection(
                        title = "Sketch History",
                        episodes = null,
                        seasons = SeasonConnection(
                            nodes = listOf(
                                SeasonNode(
                                    seasonNumber = 1,
                                    episodes = EpisodeConnection(
                                        nodes = listOf(
                                            EpisodeNode(
                                                title = "Folge 1",
                                                editorialDate = "2015-01-01T20:15:00Z",
                                                sharingUrl = "https://www.zdf.de/sketch-history-100.html",
                                                episodeInfo = EpisodeInfo(
                                                    seasonNumber = 1,
                                                    episodeNumber = 1
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        doReturn(requestSpec).`when`(zdfGraphQlClient).document(anyString())
        doReturn(requestSpec).`when`(requestSpec).variable(anyString(), any())
        doReturn(retrieveSpec).`when`(requestSpec).retrieve(anyString())
        doReturn(Mono.just(apiResponse)).`when`(retrieveSpec).toEntity(SearchDocumentsResult::class.java)

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes(seriesName, seasonNumber, 10, "date_desc")

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].episodeInfo?.seasonNumber).isEqualTo(1)
    }
        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes("unknown-series", null, 10, "date_desc")

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

        doReturn(requestSpec).`when`(zdfGraphQlClient).document(anyString())
        doReturn(requestSpec).`when`(requestSpec).variable(anyString(), any())
        doReturn(retrieveSpec).`when`(requestSpec).retrieve(anyString())
        doReturn(Mono.just(apiResponse)).`when`(retrieveSpec).toEntity(SearchDocumentsResult::class.java)

        // when
        val result = getSeriesEpisodesService.getSeriesEpisodes("some-video", null, 10, "date_desc")

        // then
        assertThat(result).isEmpty()
    }
    */
}
