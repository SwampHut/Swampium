package io.reactant.reactant.extra.parser

import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import io.reactant.reactant.core.reactantobj.container.Reactant
import io.reactant.reactant.service.spec.parser.TomlParserService
import io.reactivex.Single
import kotlin.reflect.KClass

@Reactant
class Toml4jTomlParserService : TomlParserService {
    override fun encode(obj: Any): Single<String> = Single.defer { Single.just(TomlWriter().write(obj)) }

    override fun <T : Any> decode(modelClass: KClass<T>, encoded: String): Single<T> =
            Single.defer { Single.just(Toml().read(encoded).to(modelClass.java)) }

}