package io.github.leonardootto.allowlist

import com.fasterxml.jackson.databind.ObjectMapper
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import jakarta.annotation.PostConstruct
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Service

@Service
class AllowListService(private val objectMapper: ObjectMapper) {

    private val lists = mutableMapOf<String, LongOpenHashSet>()

    @PostConstruct
    fun load() {
        val resolver = PathMatchingResourcePatternResolver()
        resolver.getResources("classpath:lists/*.json").forEach { resource ->
            val name = resource.filename!!.removeSuffix(".json")
            val pvs = objectMapper.readTree(resource.inputStream)
                .get("pvs")
                .map { it.longValue() }
            lists[name] = LongOpenHashSet(pvs)
        }
    }

    fun contains(listName: String, pv: Long): Boolean? = lists[listName]?.contains(pv)
}
