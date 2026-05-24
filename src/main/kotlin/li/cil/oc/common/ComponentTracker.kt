package li.cil.oc.common

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import li.cil.oc.api.network.ManagedEnvironment
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

/**
 * Keeps track of loaded components by ID. Used to send messages between
 * component representation on server and client without knowledge of their
 * containers. For now this is only used for screens / text buffer components.
 */
abstract class ComponentTracker {
    private val worlds: MutableMap<ResourceKey<Level>, Cache<String, ManagedEnvironment>> = mutableMapOf()

    private fun components(level: Level): Cache<String, ManagedEnvironment> {
        return worlds.getOrPut(level.dimension()) {
            @Suppress("UNCHECKED_CAST")
            CacheBuilder.newBuilder()
                .weakValues()
                .build<String, ManagedEnvironment>()
        }
    }

    fun add(level: Level, address: String, component: ManagedEnvironment) {
        synchronized(this) {
            components(level).put(address, component)
        }
    }

    fun remove(level: Level, component: ManagedEnvironment) {
        synchronized(this) {
            val cache = components(level)
            val keysToInvalidate = cache.asMap().entries
                .filter { it.value == component }
                .map { it.key }
            cache.invalidateAll(keysToInvalidate)
            cache.cleanUp()
        }
    }

    fun get(level: Level, address: String): ManagedEnvironment? {
        synchronized(this) {
            components(level).cleanUp()
            return components(level).getIfPresent(address)
        }
    }

    @SubscribeEvent
    fun onWorldUnload(e: LevelEvent.Unload) {
        val level = e.level
        if (level is Level) {
            clear(level)
        }
    }

    protected open fun clear(level: Level) {
        synchronized(this) {
            components(level).invalidateAll()
            components(level).cleanUp()
        }
    }
}
