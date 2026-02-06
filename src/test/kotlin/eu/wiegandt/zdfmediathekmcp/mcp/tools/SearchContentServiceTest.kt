package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.mcp.pagination.McpPaginationPayloadHandler
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResult
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SearchContentServiceTest {

    @Mock
    private lateinit var zdfMediathekClient: ZdfMediathekClient

    @InjectMocks
    private lateinit var searchContentService: SearchContentService

    @Test
    fun searchContent_emptyQuery_throwsException() {
        // when & then
        assertThatThrownBy {
            searchContentService.searchContent("")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("query")
            .hasMessageContaining("must not be empty")
    }

    @Test
    fun searchContent_blankQuery_throwsException() {
        // when & then
        assertThatThrownBy {
            searchContentService.searchContent("   ")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("query")
    }

    @Test
    fun searchContent_validQuery_returnsCorrectResult() {
        // given
        val zdfSearchResponse = ZdfSearchResponse()
        Mockito.doReturn(zdfSearchResponse).`when`(zdfMediathekClient).searchDocuments("heute-show 19. Dezember", 5)

        // when
        val result = searchContentService.searchContent("heute-show 19. Dezember")

        // then - assert observable behaviour only
        Assertions.assertThat(result.resources).isEmpty()
        Assertions.assertThat(result.nextCursor).isNull()
    }

    @Test
    fun searchContent_withCursor_usesDecodedPage_and_setsNextCursor() {
        // given
        val query = "test-query"
        val limit = 3
        // encode cursor with page=2
        val cursor = McpPaginationPayloadHandler.encode(2, limit)

        // prepare response with results size equal to limit to force nextCursor
        val results = (1..limit).map { i -> ZdfSearchResult(score = 1.0, id = "id$i", type = "type", title = "title$i") }
        val apiResponse = ZdfSearchResponse(totalResultsCount = 10, next = null, results = results)

        Mockito.doReturn(apiResponse).`when`(zdfMediathekClient).searchDocuments(query, limit, 2)

        // when
        val result = searchContentService.searchContent(query, limit, cursor)

        // then
        assertThat(result.resources).hasSize(limit)
        assertThat(result.nextCursor).isNotNull
        val decoded = McpPaginationPayloadHandler.decode(result.nextCursor!!)
        assertThat(decoded.page).isEqualTo(3) // next page should be current page + 1
        assertThat(decoded.limit).isEqualTo(limit)
    }

    @Test
    fun searchContent_withInvalidCursor_throwsIllegalArgumentException() {
        // given
        val invalidCursor = "not-a-valid-base64"

        // when & then
        Assertions.assertThatThrownBy {
            searchContentService.searchContent("q", 5, invalidCursor)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

}