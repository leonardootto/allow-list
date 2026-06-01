package io.github.leonardootto.allowlist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AllowListServiceTest {

    @Autowired
    lateinit var service: AllowListService

    // behavior 5
    @Test
    fun `contains returns true for pv in list`() {
        assertThat(service.contains("test-list", 1L)).isTrue()
    }

    // behavior 6
    @Test
    fun `contains returns false for pv not in list`() {
        assertThat(service.contains("test-list", 9999L)).isFalse()
    }

    // behavior 7
    @Test
    fun `contains returns null for unknown list`() {
        assertThat(service.contains("nonexistent", 1L)).isNull()
    }
}
