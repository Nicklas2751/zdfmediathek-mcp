package eu.wiegandt.zdfmediathekmcp

import com.github.tomakehurst.wiremock.client.WireMock.*
import eu.wiegandt.zdfmediathekmcp.config.HttpServicesConfiguration
import eu.wiegandt.zdfmediathekmcp.model.ZdfDocument
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfSearchResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import java.time.OffsetDateTime


@SpringBootTest(
    classes = [
        HttpServicesConfiguration::class,
        WebClientAutoConfiguration::class,
    ],
    properties = [
        "zdf.clientId=test-client",
        "zdf.clientSecret=test-secret"
    ]
)
@EnableWireMock(
    ConfigureWireMock(
        baseUrlProperties = ["zdf.url"]
    )
)
class ZdfMediathekServiceIT {

    @Autowired
    lateinit var zdfMediathekService: ZdfMediathekService

    @Test
    fun searchDocuments_validQuery_returnsResults() {
        // given
        val expectedResponse = ZdfSearchResponse(
            3104,
            "/search/documents?q=Tagesschau&limit=2&page=2",
            listOf(
                ZdfSearchResult(
                    38.0424,
                    "SCMS_index-page-ard-collection_ard_dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-",
                    "page-index",
                    "tagesschau",
                    "default",
                    ZdfDocument(
                        id = "collection-index-page-ard-collection-ard-dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-1042",
                        externalId = "SCMS_index-page-ard-collection_ard_dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-",
                        title = null,
                        teasertext = "Die Tagesschau ist die älteste und meistgesehene Nachrichtensendung im deutschen Fernsehen. Bis heute der Inbegriff für aktuelle Nachrichten. Seriös und auf den Punkt.",
                        editorialDate = OffsetDateTime.parse("2025-12-26T13:37:23.220Z"),
                        contentType = "brand",
                        hasVideo = false,
                        webCanonical = "https://www.zdf.de/magazine/collection-index-page-ard-collection-ard-dxjuomfyzdpzag93ojvindnjy2njm2q5oddinde-1042"
                    )
                ),
                ZdfSearchResult(
                    score = 32.49361,
                    id = "SCMS_page-video-ard_video_ard_dXJuOmFyZDpwdWJsaWNhdGlvbjplM2Y3MmZkYzhjZWM5MDM5",
                    type = "page-video",
                    title = "tagesschau",
                    resultType = "default",
                    target = ZdfDocument(
                        id = "page-video-ard-tagesschau-104",
                        externalId = "SCMS_page-video-ard_video_ard_dXJuOmFyZDpwdWJsaWNhdGlvbjplM2Y3MmZkYzhjZWM5MDM5",
                        title = null,
                        teasertext = "Verfassungsschutz sieht wachsenden Einfluss verfassungsfeindlicher Strömungen in AfD, Brennender Frachter \"Fremantle Highway\" liegt windgeschützt vor Anker, Mit der Aktion \"Wahre Kosten\" will der Discounter Penny beispielhaft auf die negative Umweltbilanz einiger Lebensmittel hinweisen, Immer mehr Menschen in Deutschland müssen beim Essen sparen, Solarpark mit Speicherkapazität in Baden-Württemberg eröffnet, Verkehrsminister Wissing beauftragt Rechtsgutachten zu Mautverträgen des Ex-Verkehrsministers Scheuer, Schweden und Dänemark planen Koranverbrennungen auf juristischem Weg zu verhindern, Ergebnisse Fußball-WM der Frauen, Para-Schwimm-WM, Das Wetter Hinweis: Der Beitrag zur \"Fußball-WM der Frauen: Australien gewinnt gegen Kanada\" darf aus rechtlichen Gründen nicht auf tagesschau.de gezeigt werden. Korrektur: Diese Sendung wurde nachträglich bearbeitet. Der Beitrag „Wahre Kosten“ wurde nachträglich bearbeitet. In der ursprünglichen Version gab es eine O-Ton-Geberin, die für den WDR arbeitet. Die mit ihr gezeigte Sequenz hätte so nicht gesendet werden dürfen. Kolleginnen oder Kollegen zu interviewen entspricht nicht unseren journalistischen Standards. Ergänzung: Die O-Ton-Geberin war zufällig als Kundin in diesem Discounter.\"",
                        editorialDate = OffsetDateTime.parse("2023-07-31T18:42:32.054Z"),
                        contentType = "episode",
                        hasVideo = true,
                        webCanonical = "https://www.ardmediathek.de/video/Y3JpZDovL3RhZ2Vzc2NoYXUuZGUvMjc2OGEwODgtM2E0ZC00MWIwLWJjMDItMTVhZjM3NTgwZDY1X2dhbnplU2VuZHVuZw",
                        tvService = "daserste",
                        endDate = OffsetDateTime.parse("2099-01-01T00:00:00.092Z")
                    )
                )
            )
        )

        stubFor(
            get(urlPathEqualTo("/search/documents"))
                .withQueryParam("q", equalTo("Tagesschau"))
                .withQueryParam("limit", equalTo("2"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("search_documents.json")
                )
        )


        // when
        val response = zdfMediathekService.searchDocuments("Tagesschau", 2)

        // then
        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse)
    }

    @Test
    fun searchDocuments_withOAuth2_sendsAuthorizationHeader() {
        // given
        stubFor(
            post(urlPathEqualTo("/oauth/token"))
                .withBasicAuth("test-client", "test-secret")
                .withRequestBody(containing("grant_type=client_credentials"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"mock-token","token_type":"Bearer","expires_in":3600}""")
                )
        )

        stubFor(
            get(urlPathEqualTo("/search/documents"))
                .withHeader("Authorization", equalTo("Bearer mock-token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("search_documents.json")
                )
        )

        // when
        val response = zdfMediathekService.searchDocuments(q = "Tagesschau", limit = 2)

        // then
        assertThat(response.results).isNotEmpty
        verify(postRequestedFor(urlPathEqualTo("/oauth/token")))
        verify(
            getRequestedFor(urlPathEqualTo("/search/documents"))
                .withHeader("Authorization", equalTo("Bearer mock-token"))
        )
    }

}