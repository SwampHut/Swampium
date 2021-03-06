package dev.reactant.reactant.core.dependency.injection.components

import dev.reactant.reactant.core.dependency.implied.ImpliedDepend

@ImpliedDepend(nullableArgumentIndexes = [0])
class Components<T>(private val components: List<T>) : List<T> by components

