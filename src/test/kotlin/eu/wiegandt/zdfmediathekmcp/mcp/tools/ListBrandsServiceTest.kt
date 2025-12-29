package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.BrandApiResponse
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.web.reactive.function.client.WebClientResponseException

class ListBrandsServiceTest {
    private val zdfMediathekClient = Mockito.mock(ZdfMediathekClient::class.java)
    private val listBrandsService = ListBrandsService(zdfMediathekClient)

    @Test
    fun `listBrands returns expected brands`() {
        // Given
        val brands = listOf(
            BrandSummary("id1", "Terra X", "Doku-Reihe"),
            BrandSummary("id2", "Tatort", "Krimi")
        )
        BDDMockito.given(zdfMediathekClient.listBrands(10)).willReturn(BrandApiResponse(brands))
        // When
        val result = listBrandsService.listBrands()
        // Then
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(brands)
    }

    @Test
    fun `listBrands with empty result returns empty list`() {
        // Given
        BDDMockito.given(zdfMediathekClient.listBrands(10)).willReturn(BrandApiResponse(emptyList()))
        // When
        val result = listBrandsService.listBrands()
        // Then
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(emptyList<BrandSummary>())
    }

    @Test
    fun `listBrands with limit parameter passes limit`() {
        // Given
        BDDMockito.given(zdfMediathekClient.listBrands(5)).willReturn(BrandApiResponse(emptyList()))
        // When
        listBrandsService.listBrands(5)
        // Then
        Mockito.verify(zdfMediathekClient).listBrands(5)
    }

    @Test
    fun `listBrands throws on API error`() {
        // Given
        Mockito.doThrow(
            WebClientResponseException(
                500,
                "Server Error",
                null,
                null,
                null
            )
        ).`when`(zdfMediathekClient).listBrands(10)
        // When/Then
        Assertions.assertThatThrownBy { listBrandsService.listBrands() }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Failed to list brands: 500 Server Error")
    }
}