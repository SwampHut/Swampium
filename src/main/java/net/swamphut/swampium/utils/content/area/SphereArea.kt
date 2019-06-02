package net.swamphut.swampium.utils.content.area

import net.swamphut.swampium.extensions.plus
import org.bukkit.util.Vector

open class SphereArea(var center: Vector, var radius: Double) : Area {
    override fun move(vector: Vector) {
        this.center += vector
    }

    override fun contains(vec: Vector): Boolean = vec.isInSphere(center, radius)

    override fun clone(): SphereArea = SphereArea(center, radius)

}
