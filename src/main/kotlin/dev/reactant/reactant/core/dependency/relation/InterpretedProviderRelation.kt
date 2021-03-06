package dev.reactant.reactant.core.dependency.relation

import dev.reactant.reactant.core.dependency.injection.InjectRequirement
import dev.reactant.reactant.core.dependency.injection.producer.Provider

class InterpretedProviderRelation(
        val interpretBy: Any,
        val interpretTarget: Provider,
        val dependOn: Provider,
        val reason: String,
        val resolvedRequirements: Set<Pair<InjectRequirement, Provider>> = setOf(),
        val priority: Int = 0
) : Comparable<InterpretedProviderRelation> {
    /**
     * For those no resolved requirement relation, it is a direct relation
     */
    val directRelation get() = resolvedRequirements.size == 0

    override fun toString(): String {
        return "{ interpretedBy: ${interpretBy.javaClass.canonicalName}, provider: ${interpretTarget.productType}, dependOn: ${dependOn.productType}," +
                " reason: ${reason}, resolvedRequirements: ${resolvedRequirements.map { it.first.toString() }.joinToString(",")} }"
    }

    override fun compareTo(other: InterpretedProviderRelation): Int {
        return priority.compareTo(other.priority)
    }
}
