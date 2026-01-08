package eu.wiegandt.zdfmediathekmcp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "zdf.client.id=test-client-id",
    "zdf.client.secret=test-client-secret"
])
class ZdfMediathekMcpApplicationTests {

    @Test
    fun contextLoads() {
    }

}
