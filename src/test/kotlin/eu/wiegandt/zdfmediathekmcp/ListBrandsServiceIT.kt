package eu.wiegandt.zdfmediathekmcp

import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.model.BrandSummary
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

@SpringBootTest(
    properties = [
        "zdf.client.id=test-client",
        "zdf.client.secret=test-secret"
    ]
)
@EnableWireMock(
    ConfigureWireMock(
        baseUrlProperties = ["zdf.url"]
    )
)
class ListBrandsServiceIT {

    @Autowired
    lateinit var searchBrandsService: SearchBrandsService

    @Test
    fun `listBrands returns mapped brands from API`() {
        // Given: OAuth und Brands-API werden gemockt
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )
        stubFor(
            get(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("10"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                              "brands": [
                                {
                                  "uuid": "id1",
                                  "brandName": "Terra X",
                                  "brandDescription": "Doku-Reihe"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                )
        )
        val expected = listOf(
            BrandSummary(
                uuid = "id1",
                brandName = "Terra X",
                brandDescription = "Doku-Reihe"
            )
        )
        // When: Service wird aufgerufen
        val result = searchBrandsService.listBrands()
        // Then: Ergebnis entspricht Erwartung, Request wurde korrekt abgesetzt
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
        verify(
            getRequestedFor(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("10"))
        )
    }

    @Test
    fun `listBrands with limit parameter passes limit`() {
        // Given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )
        stubFor(
            get(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("5"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"brands":[]}""")
                )
        )
        val expected = emptyList<BrandSummary>()
        // When
        val result = searchBrandsService.listBrands(5)
        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
        verify(
            getRequestedFor(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("5"))
        )
    }

    @Test
    fun `listBrands when API returns 401 throws exception`() {
        // Given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error":"invalid_client"}""")
                )
        )
        // When/Then
        assertThatThrownBy {
            searchBrandsService.listBrands()
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `listBrands when API returns 500 throws exception`() {
        // Given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
        )
        stubFor(
            get(urlPathEqualTo("/cmdm/brands"))
                .withQueryParam("limit", equalTo("10"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error":"Internal Server Error"}""")
                )
        )
        // When/Then
        assertThatThrownBy {
            searchBrandsService.listBrands()
        }.isInstanceOf(RuntimeException::class.java)
    }
}
