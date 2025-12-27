package eu.wiegandt.zdfmediathekmcp

import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SearchContentServiceTest {

    @Mock
    private lateinit var zdfMediathekService: ZdfMediathekService

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
        doReturn(zdfSearchResponse).`when`(zdfMediathekService).searchDocuments("heute-show 19. Dezember", 5)

        // when
        val results = searchContentService.searchContent("heute-show 19. Dezember")

        // then
        assertThat(results).isEqualTo(zdfSearchResponse)
    }

}