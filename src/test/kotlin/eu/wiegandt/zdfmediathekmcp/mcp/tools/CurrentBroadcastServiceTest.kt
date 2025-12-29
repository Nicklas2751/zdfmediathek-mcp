package eu.wiegandt.zdfmediathekmcp.mcp.tools

import eu.wiegandt.zdfmediathekmcp.ZdfMediathekClient
import eu.wiegandt.zdfmediathekmcp.model.CurrentBroadcastResponse
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcast
import eu.wiegandt.zdfmediathekmcp.model.ZdfBroadcastScheduleResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime
import java.time.ZoneId

class CurrentBroadcastServiceTest {

    private val zdfMediathekClient: ZdfMediathekClient = mock(ZdfMediathekClient::class.java)
    private val currentBroadcastService = CurrentBroadcastService(zdfMediathekClient)

    @Test
    fun `getCurrentBroadcast with valid channel returns current broadcast`() {
        // given
        val now = OffsetDateTime.now(ZoneId.of("Europe/Berlin"))
        val tvService = "ZDF"
        val currentBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusMinutes(30),
            airtimeEnd = now.plusMinutes(30),
            duration = 3600,
            tvService = tvService,
            title = "Aktuell laufende Sendung",
            subtitle = null,
            text = "Dies ist die aktuell laufende Sendung",
            programmeItem = null
        )
        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = listOf(currentBroadcast),
            nextArchive = null
        )

        `when`(zdfMediathekClient.getCurrentBroadcastSchedule(tvService))
            .thenReturn(mockResponse)

        // when
        val result = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(result).isEqualTo(
            CurrentBroadcastResponse(
                tvService = tvService,
                currentBroadcast = currentBroadcast,
                queriedAt = result.queriedAt
            )
        )
    }

    @Test
    fun `getCurrentBroadcast with empty channel name throws exception`() {
        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast("")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Parameter 'tvService' is required and must not be empty")
    }

    @Test
    fun `getCurrentBroadcast with blank channel name throws exception`() {
        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast("   ")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Parameter 'tvService' is required and must not be empty")
    }

    @Test
    fun `getCurrentBroadcast with no broadcast found returns null`() {
        // given
        val tvService = "ZDF"
        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = emptyList(),
            nextArchive = null
        )

        `when`(zdfMediathekClient.getCurrentBroadcastSchedule(tvService))
            .thenReturn(mockResponse)

        // when
        val result = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(result).isEqualTo(
            CurrentBroadcastResponse(
                tvService = tvService,
                currentBroadcast = null,
                queriedAt = result.queriedAt
            )
        )
    }

    @Test
    fun `getCurrentBroadcast with multiple broadcasts returns current one`() {
        // given
        val now = OffsetDateTime.now(ZoneId.of("Europe/Berlin"))
        val tvService = "ZDF"

        val pastBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusHours(2),
            airtimeEnd = now.minusHours(1),
            duration = 3600,
            tvService = tvService,
            title = "Vergangene Sendung",
            subtitle = null,
            text = "Diese Sendung ist vorbei",
            programmeItem = null
        )

        val currentBroadcast = ZdfBroadcast(
            airtimeBegin = now.minusMinutes(30),
            airtimeEnd = now.plusMinutes(30),
            duration = 3600,
            tvService = tvService,
            title = "Aktuell laufende Sendung",
            subtitle = null,
            text = "Dies ist die aktuell laufende Sendung",
            programmeItem = null
        )

        val futureBroadcast = ZdfBroadcast(
            airtimeBegin = now.plusHours(1),
            airtimeEnd = now.plusHours(2),
            duration = 3600,
            tvService = tvService,
            title = "Zukünftige Sendung",
            subtitle = null,
            text = "Diese Sendung läuft später",
            programmeItem = null
        )

        val mockResponse = ZdfBroadcastScheduleResponse(
            broadcasts = listOf(pastBroadcast, currentBroadcast, futureBroadcast),
            nextArchive = null
        )

        `when`(zdfMediathekClient.getCurrentBroadcastSchedule(tvService))
            .thenReturn(mockResponse)

        // when
        val result = currentBroadcastService.getCurrentBroadcast(tvService)

        // then
        assertThat(result.currentBroadcast).isNotNull()
        assertThat(result.currentBroadcast?.title).isEqualTo("Aktuell laufende Sendung")
    }

    @Test
    fun `getCurrentBroadcast when API throws exception wraps exception`() {
        // given
        val tvService = "ZDF"
        `when`(zdfMediathekClient.getCurrentBroadcastSchedule(tvService))
            .thenThrow(RuntimeException("API Error"))

        // when / then
        assertThatThrownBy {
            currentBroadcastService.getCurrentBroadcast(tvService)
        }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Failed to get current broadcast")
    }

}

