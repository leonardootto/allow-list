package io.github.leonardootto.allowlist

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class AllowListControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    // behavior 1 — tracer bullet
    @Test
    fun `hit returns 200 with allowed true`() {
        mvc.get("/allow-list/test-list?pv=1")
            .andExpect {
                status { isOk() }
                jsonPath("$.allowed") { value(true) }
            }
    }

    // behavior 2
    @Test
    fun `miss returns 200 with allowed false`() {
        mvc.get("/allow-list/test-list?pv=9999")
            .andExpect {
                status { isOk() }
                jsonPath("$.allowed") { value(false) }
            }
    }

    // behavior 3
    @Test
    fun `unknown list returns 404`() {
        mvc.get("/allow-list/nonexistent?pv=1")
            .andExpect {
                status { isNotFound() }
            }
    }

    // behavior 4
    @Test
    fun `invalid pv returns 400`() {
        mvc.get("/allow-list/test-list?pv=abc")
            .andExpect {
                status { isBadRequest() }
            }
    }

    // behavior 5 — US11: readiness probe
    @Test
    fun `actuator health returns 200`() {
        mvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
            }
    }
}
