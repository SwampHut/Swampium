package dev.reactant.reactant.utils.content.area

import dev.reactant.reactant.extensions.locationOf
import org.bukkit.Location
import org.bukkit.World

open class WorldArea<out T : Area>(var world: World, val area: T) : Area by area {
    override fun clone(): WorldArea<T> = WorldArea(world, area)
    override fun contains(loc: Location): Boolean = loc.world == world && super.contains(loc)

    val entities get() = world.entities.filter { it in this }
    val livingEntities get() = world.livingEntities.filter { it in this }
    val players get() = world.players.filter { it in this }

    @Suppress("UNCHECKED_CAST")
    fun toArea(): T = area.clone() as T

    /**
     * Get the list of area included integer locations (as know as block location)
     */
    val integerLocations: List<Location> get() = area.integerVectors.map { locationOf(world, it) }
}
