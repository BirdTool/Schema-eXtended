package studio.styx.sx.core

/*
import studio.styx.sx.types.Schema

@Suppress("UNCHECKED_CAST")
class ArraySchema<T>(
    private val itemSchema: String? = null,
    internal open val errorMessage: String? = null,
    internal open val coerce: Boolean = false
) : Schema<List<T>> {

    private var minLength: Int? = null
    private var maxLength: Int? = null
    private var defaultValue: List<T>? = null
    private var minLengthErrorMessage: String? = null
    private var maxLengthErrorMessage: String? = null

    fun min(length: Int, errorMessage: String? = null) = apply {
        minLength = length
        minLengthErrorMessage = errorMessage
    }

    fun max(length: Int, errorMessage: String? = null) = apply {
        maxLength = length
        maxLengthErrorMessage = errorMessage
    }

    fun nonempty(errorMessage: String? = null) = min(1, errorMessage)

    fun default(value: List<T>) = apply { defaultValue = value }

    override fun safeParse(input: Any?): SafeParseResult<List<T>> {
        val errors = mutableListOf<String>()
        var parsedList: List<T>? = null

        // 1. Null / vazio → default
        if (input == null || input == "" || (input is String && input.trim().isEmpty())) {
            if (defaultValue != null) {
                parsedList = defaultValue
            } else {
                errors.add(errorMessage ?: "Value is required")
                return SafeParseResult.failure(errors)
            }
        } else {
            // 2. Coerce para List<Any?>
            val rawList = if (coerce) coerceToList(input) else input.toRawList()

            if (rawList == null) {
                errors.add(errorMessage ?: "Expected an array, got ${input!!::class.simpleName}")
                return SafeParseResult.failure(errors)
            }

            // 3. Validar itens com schema (se houver)
            parsedList = if (itemSchema != null) {
                val resultList = mutableListOf<T>()
                rawList.forEachIndexed { index, item ->
                    val result = itemSchema.safeParse(item)
                    if (result.success) {
                        result.value?.let { resultList.add(it) }
                    } else {
                        errors.add("[$index]: ${result.errors.joinToString(" | ")}")
                    }
                }
                if (errors.isNotEmpty()) return SafeParseResult.failure(errors)
                resultList
            } else {
                rawList as List<T> // sem schema → aceita qualquer coisa
            }
        }

        // 4. Validações de tamanho
        val value = parsedList!!

        minLength?.let { min ->
            if (value.size < min) {
                errors.add(minLengthErrorMessage ?: "Array too short: ${value.size} < $min")
            }
        }

        maxLength?.let { max ->
            if (value.size > max) {
                errors.add(maxLengthErrorMessage ?: "Array too long: ${value.size} > $max")
            }
        }

        return (if (errors.isEmpty()) {
            SafeParseResult.success(value)
        } else {
            SafeParseResult.failure(errors)
        }) as SafeParseResult<List<T>>
    }

    // Helpers
    private fun Any?.toRawList(): List<Any?>? = when (this) {
        is List<*> -> this
        is Array<*> -> this.toList()
        is Iterable<*> -> this.toList()
        else -> null
    }

    private fun coerceToList(input: Any?): List<Any?>? {
        return when (input) {
            is String -> {
                val trimmed = input.trim()
                if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
                    // JSON array simples
                    try {
                        val content = trimmed.drop(1).dropLast(1)
                        if (content.isBlank()) return emptyList()
                        content.split(',').map {
                            it.trim().removeSurrounding("\"").removeSurrounding("'")
                        }
                    } catch (e: Exception) { null }
                } else {
                    // CSV
                    trimmed.split(',').map { it.trim() }
                }
            }
            else -> input.toRawList()
        }
    }
}

 */