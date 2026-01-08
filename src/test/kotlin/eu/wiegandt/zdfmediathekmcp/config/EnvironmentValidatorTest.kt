package eu.wiegandt.zdfmediathekmcp.config

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EnvironmentValidatorTest {

    @Test
    fun `should pass validation with valid credentials`() {
        // given
        val validator = EnvironmentValidator(
            clientId = "valid-client-id",
            clientSecret = "valid-client-secret"
        )

        // when & then - should not throw exception
        validator.validateEnvironment()
    }

    @Test
    fun `should fail validation with blank client id`() {
        // given
        val validator = EnvironmentValidator(
            clientId = "",
            clientSecret = "valid-client-secret"
        )

        // when & then
        assertThatThrownBy { validator.validateEnvironment() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("ZDF_CLIENT_ID")
    }

    @Test
    fun `should fail validation with blank client secret`() {
        // given
        val validator = EnvironmentValidator(
            clientId = "valid-client-id",
            clientSecret = ""
        )

        // when & then
        assertThatThrownBy { validator.validateEnvironment() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("ZDF_CLIENT_SECRET")
    }

    @Test
    fun `should fail validation with default client id`() {
        // given
        val validator = EnvironmentValidator(
            clientId = "mediathek-search",
            clientSecret = "valid-client-secret"
        )

        // when & then
        assertThatThrownBy { validator.validateEnvironment() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("ZDF_CLIENT_ID")
            .hasMessageContaining("default value")
    }

    @Test
    fun `should fail validation with default client secret`() {
        // given
        val validator = EnvironmentValidator(
            clientId = "valid-client-id",
            clientSecret = "ZDFmediathekSearchClientSecret"
        )

        // when & then
        assertThatThrownBy { validator.validateEnvironment() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("ZDF_CLIENT_SECRET")
            .hasMessageContaining("default value")
    }

    @Test
    fun `should fail validation with both credentials invalid`() {
        // given
        val validator = EnvironmentValidator(
            clientId = "",
            clientSecret = ""
        )

        // when & then
        assertThatThrownBy { validator.validateEnvironment() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("ZDF_CLIENT_ID")
            .hasMessageContaining("ZDF_CLIENT_SECRET")
    }
}

