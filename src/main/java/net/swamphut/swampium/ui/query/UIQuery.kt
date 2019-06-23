package net.swamphut.swampium.ui.query

import net.swamphut.swampium.ui.element.UIElement
import net.swamphut.swampium.ui.element.UIElementChildren

interface UIQueryable {
    val children: UIElementChildren

    /**
     * Find by predicate, return first or null
     */
    fun UIQueryable.firstElement(predicate: (UIElement) -> Boolean): UIElement? =
            children.firstOrNull(predicate) ?: children.mapNotNull { it.firstElement(predicate) }.firstOrNull()

    fun getElementById(id: String): UIElement? = firstElement { it.id == id }
}

inline fun <reified T : UIElement> UIQueryable.getElementById(id: String): T? = getElementById(id) as T?
