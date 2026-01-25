package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
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
        assertThat(result.resources).isEmpty()
        assertThat(result.nextCursor).isNull()
    }

}