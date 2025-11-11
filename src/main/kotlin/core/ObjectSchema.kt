package studio.styx.sx.core

import studio.styx.sx.types.Schema
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ObjectSchema<T : Any>(
    private val shape: Map<String, Schema<*>>,
    private val constructor: ((Map<String, Any?>) -> T)? = null,
    internal open val errorMessage: String? = null,
    internal open val coerce: Boolean = false
) : Schema<T> {

    private var strictMode = false
    private var stripUnknown = false
    private var defaultValue: T? = null

    fun strict() = apply { strictMode = true }
    fun strip() = apply { stripUnknown = true }
    fun default(value: T) = apply { defaultValue = value }

    override fun safeParse(input: Any?): SafeParseResult<T> {
        val errors = mutableListOf<String>()

        if (input == null || input == "") {
            return defaultValue?.let { SafeParseResult.success(it) }
                ?: SafeParseResult.failure(errorMessage ?: "Value is required")
        }

        val map = when {
            coerce -> input.coerceToMap()
            input is Map<*, *> -> input as Map<String, Any?>
            else -> {
                errors.add(errorMessage ?: "Expected an object, got ${input::class.simpleName}")
                return SafeParseResult.failure(errors)
            }
        } ?: run {
            errors.add("Cannot coerce to object")
            return SafeParseResult.failure(errors)
        }

        val result = mutableMapOf<String, Any?>()

        for ((key, schema) in shape) {
            val value = map[key]
            val parseResult = schema.safeParse(value)

            if (parseResult.success) {
                result[key] = parseResult.value
            } else {
                errors.add(".$key: ${parseResult.errors.joinToString(" | ")}")
            }
        }

        if (strictMode) {
            val extra = map.keys - shape.keys
            if (extra.isNotEmpty()) errors.add("Unknown keys: ${extra.joinToString(", ")}")
        }

        if (stripUnknown) {
            result.putAll(map.filterKeys { it in shape.keys })
        }

        if (errors.isNotEmpty()) {
            return SafeParseResult.failure(errors)
        }

        // AQUI ESTÁ A MÁGICA: usa o constructor se tiver, senão retorna o Map
        return try {
            val instance = constructor?.invoke(result)
                ?: result as? T
                ?: result as T // se T for Map<String, Any?>, funciona direto

            SafeParseResult.success(instance)
        } catch (e: Exception) {
            SafeParseResult.failure("Failed to construct object: ${e.message}")
        }
    }

    override fun parse(input: Any?): T =
        safeParse(input).let { r ->
            if (!r.success) throw IllegalArgumentException(r.errors.joinToString(" | "))
            r.value!!
        }

    override fun parseOptional(input: Any?): T? = safeParse(input).value
}

// ========= COERCE PARA MAP =========
private fun Any?.coerceToMap(): Map<String, Any?>? = when (this) {
    is String -> {
        val trimmed = trim()
        if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
            // JSON object simples
            try {
                val content = trimmed.drop(1).dropLast(1)
                val map = mutableMapOf<String, Any?>()
                var key = StringBuilder()
                var value = StringBuilder()
                var inKey = true
                var inQuotes = false
                var quoteChar = '"'

                for (char in content + ',') {
                    when {
                        char in "\"'" && key.isEmpty() && inKey -> {
                            inQuotes = true
                            quoteChar = char
                        }
                        char == quoteChar && inQuotes -> inQuotes = false
                        char == ':' && !inQuotes && inKey -> {
                            inKey = false
                        }
                        char == ',' && !inQuotes && !inKey -> {
                            map[key.toString().trim().removeSurrounding("\"").removeSurrounding("'")] = value.toString().trim().removeSurrounding("\"").removeSurrounding("'")
                            key = StringBuilder()
                            value = StringBuilder()
                            inKey = true
                        }
                        else -> if (inKey) key.append(char) else value.append(char)
                    }
                }
                map
            } catch (e: Exception) { null }
        } else null
    }
    else -> null
}