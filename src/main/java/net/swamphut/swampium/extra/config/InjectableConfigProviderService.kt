package net.swamphut.swampium.extra.config

import net.swamphut.swampium.core.dependency.injection.Provide
import net.swamphut.swampium.core.dependency.injection.lazy.LazyInjection
import net.swamphut.swampium.core.swobject.container.SwObject
import net.swamphut.swampium.service.spec.config.Config
import net.swamphut.swampium.service.spec.config.ConfigService
import net.swamphut.swampium.service.spec.parser.JsonParserService
import net.swamphut.swampium.service.spec.parser.ParserService
import net.swamphut.swampium.service.spec.parser.TomlParserService
import net.swamphut.swampium.service.spec.parser.YamlParserService
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

@SwObject
private class InjectableConfigProviderService(
        private val jsonParserService: LazyInjection<JsonParserService>,
        private val yamlParserService: LazyInjection<YamlParserService>,
        private val tomlParserService: LazyInjection<TomlParserService>,
        private val configService: ConfigService
) {
    @Provide("^.*\\.(ya?ml|json|toml)$", true)
    private fun provideConfig(kType: KType, name: String): Config<Any> {
        val parser = when {
            name.matches("^.*\\.ya?ml$".toRegex()) -> yamlParserService
            name.matches("^.*\\.json$".toRegex()) -> jsonParserService
            name.matches("^.*\\.toml$".toRegex()) -> tomlParserService
            else -> throw IllegalStateException()
        }
        return getConfig(parser, kType, name)
    }

    private fun getConfig(parser: LazyInjection<out ParserService>, kType: KType, path: String): Config<Any> {
        val parserInstance = parser.get()
                ?: throw IllegalArgumentException("No provider for ${parser.ktype}")

        @Suppress("UNCHECKED_CAST")
        val configClass = kType.arguments.first().type!!.jvmErasure as KClass<Any>
        var exist = true

        return configService.loadOrDefault(parserInstance, configClass, path) {
            exist = false
            when {
                configClass.constructors.size > 1 ->
                    throw IllegalArgumentException("There have more than one constructor for config ${configClass.jvmName}")
                configClass.constructors.first().parameters.isNotEmpty() ->
                    throw IllegalArgumentException("Config constructor is not parameterless ${configClass.jvmName}")
                else -> return@loadOrDefault configClass.constructors.first().call()
            }
        }.blockingGet().also { if (!exist) it.save().blockingAwait() }
    }
}
