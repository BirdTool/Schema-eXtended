package studio.styx.sx.core

import studio.styx.sx.types.Schema

@Suppress("UNCHECKED_CAST")
class ArraySchema<T>(
    private val itemSchema: Schema<T>? = null,
    internal open val errorMessage: String? = null,
    internal open val coerce: Boolean = false
) : Schema<List<T>> {

    private var min: Int? = null
    private var max: Int? = null
    private var minErrorMessage: String? = null
    private var maxErrorMessage: String? = null
    private var defaultValue: List<T>? = null

    fun min(length: Int, errorMessage: String? = null) = apply {
        min = length
        minErrorMessage = errorMessage
    }

    fun max(length: Int, errorMessage: String? = null) = apply {
        max = length
        maxErrorMessage = errorMessage
    }

    fun default(value: List<T>) = apply { defaultValue = value }

    fun nonempty(errorMessage: String? = null) = min(1, errorMessage)

    override fun safeParse(input: Any?): SafeParseResult<List<T>> {
        val errors = mutableListOf<String>()

        // 1. Null → default ou emptyList
        if (input == null || input == "") {
            return defaultValue?.let { SafeParseResult.success(it) }
                ?: SafeParseResult.success(emptyList())
        }

        // 2. Já é List<*>? Iterable? Array? → usa direto
        val rawList: List<Any?> = when (input) {
            is List<*> -> input
            is Array<*> -> input.toList()
            is Iterable<*> -> input.toList()
            else -> if (coerce) input.coerceToList() else null
                ?: run {
                    errors.add(errorMessage ?: "Expected an array, got ${input::class.simpleName}")
                    return SafeParseResult.failure(errors)
                }
        }!!

        // 3. Validação de tamanho
        min?.let { if (rawList.size < it) errors.add(minErrorMessage ?: "Array too short: ${rawList.size} < $it") }
        max?.let { if (rawList.size > it) errors.add(maxErrorMessage ?: "Array too long: ${rawList.size} > $it") }
        if (errors.isNotEmpty()) return SafeParseResult.failure(errors)

        // 4. Validar cada item com o schema
        if (itemSchema != null) {
            val validated = mutableListOf<T>()
            rawList.forEachIndexed { index, item ->
                val result = itemSchema.safeParse(item)
                if (result.success) {
                    validated.add(result.value!!)
                } else {
                    errors.add("[$index]: ${result.errors.joinToString(" | ")}")
                }
            }
            return if (errors.isEmpty()) {
                SafeParseResult.success(validated)
            } else {
                SafeParseResult.failure(errors)
            }
        }

        // 5. Sem schema → aceita tudo
        return SafeParseResult.success(rawList as List<T>)
    }

    override fun parse(input: Any?): List<T> =
        safeParse(input).let { result ->
            if (!result.success) throw IllegalArgumentException(result.errors.joinToString(" | "))
            result.value!!
        }

    override fun parseOptional(input: Any?): List<T>? = safeParse(input).value
}

private fun Any?.coerceToList(): List<Any?>? {
    return when (this) {
        is String -> {
            val trimmed = this.trim()
            if (trimmed.isEmpty()) return emptyList()

            when {
                trimmed.startsWith('[') && trimmed.endsWith(']') -> {
                    // JSON array simples
                    val content = trimmed.drop(1).dropLast(1)
                    if (content.isBlank()) return emptyList()

                    val result = mutableListOf<String>()
                    var current = StringBuilder()
                    var inQuotes = false
                    var quoteChar = '"'

                    for (char in content + ',') { // +',' garante último item
                        when {
                            char in "\"'" && current.isEmpty() -> {
                                inQuotes = true
                                quoteChar = char
                            }

                            char == quoteChar && inQuotes -> {
                                inQuotes = false
                            }

                            char == ',' && !inQuotes -> {
                                result.add(current.toString().trim())
                                current = StringBuilder()
                            }

                            else -> current.append(char)
                        }
                    }
                    result.map { it.removeSurrounding("\"").removeSurrounding("'") }
                }

                else -> trimmed.split(',').map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
            }
        }

        else -> null
    }
}