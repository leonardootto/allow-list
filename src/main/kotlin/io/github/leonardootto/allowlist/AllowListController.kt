package io.github.leonardootto.allowlist

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AllowListController(private val service: AllowListService) {

    @GetMapping("/allow-list/{listName}")
    fun check(
        @PathVariable listName: String,
        @RequestParam pv: Long,
    ): ResponseEntity<Map<String, Boolean>> {
        val result = service.contains(listName, pv)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mapOf("allowed" to result))
    }
}
