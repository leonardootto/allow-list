package io.github.leonardootto.allowlist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AllowListApplication

fun main(args: Array<String>) {
    runApplication<AllowListApplication>(*args)
}
