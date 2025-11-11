package studio.styx.sx.core

import java.math.BigDecimal
import java.math.BigInteger
import studio.styx.sx.types.NumberSchemaType
import studio.styx.sx.types.Schema

@Suppress("UNCHECKED_CAST")
open class NumberSchema<T>(
    internal open val type: NumberSchemaType,
    internal open val errorMessage: String? = null,
    internal open val coerce: Boolean = false
): Schema<T> where T : Number, T : Comparable<T> {

    internal open var minValue: T? = null
    internal open var maxValue: T? = null
    internal open var defaultValue: T? = null
    internal open var minValueErrorMessage: String? = null
    internal open var maxValueErrorMessage: String? = null

    override fun safeParse(input: Any?): SafeParseResult<T> {
        val errors = mutableListOf<String>()
        var parsedValue: T? = null

        // 1. Null/vazio
        if (input == null || input == "") {
            if (defaultValue != null) {
                parsedValue = defaultValue
            } else {
                errors.add(errorMessage ?: "Value is required")
                return SafeParseResult.failure(errors)
            }
        } else {
            // 2. Tipo + coerce
            parsedValue = try {
                if (coerce) coerceInput(input) else input as? T
            } catch (e: IllegalArgumentException) {
                errors.add(e.message ?: "Invalid number")
                return SafeParseResult.failure(errors)
            }

            if (parsedValue == null) {
                errors.add(errorMessage ?: "Expected a ${type.name.lowercase()}, got ${input::class.simpleName}")
                return SafeParseResult.failure(errors)
            }
        }

        // 3. Validações (agora parsedValue é T non-null)
        val value = parsedValue!!

        minValue?.let { min ->
            if (value < min) {
                errors.add(minValueErrorMessage ?: "Value must be >= $min")
            }
        }

        maxValue?.let { max ->
            if (value > max) {
                errors.add(maxValueErrorMessage ?: "Value must be <= $max")
            }
        }

        return (if (errors.isEmpty()) {
            SafeParseResult.success(value)
        } else {
            SafeParseResult.failure(errors)
        }) as SafeParseResult<T>
    }

    // parse e parseOptional
    override fun parse(input: Any?): T = safeParse(input).let { r ->
        if (!r.success) throw IllegalArgumentException(r.errors.joinToString(" | "))
        r.value!!
    }

    override fun parseOptional(input: Any?): T? = safeParse(input).value

    private fun parseInternal(input: Any?, onNull: () -> T?): T? {
        if (input == null || input == "") {
            return onNull()
        }

        val result: T = if (coerce) {
            coerceInput(input)
        } else {
            input as? T ?: throw IllegalArgumentException(
                errorMessage ?: "Expected a ${type.name.lowercase()}, got ${input::class.simpleName}"
            )
        }

        if (minValue != null && result < minValue!!) {
            throw IllegalArgumentException(minValueErrorMessage ?: "Value $result must be >= $minValue")
        }
        if (maxValue != null && result > maxValue!!) {
            throw IllegalArgumentException(maxValueErrorMessage ?: "Value $result must be <= $maxValue")
        }

        return result
    }

    fun min(value: T, errorMessage: String? = null) = apply {
        minValue = value
        minValueErrorMessage = errorMessage
    }

    fun max(value: T, errorMessage: String? = null) = apply {
        maxValue = value
        maxValueErrorMessage = errorMessage
    }

    fun default(value: T) = apply {
        defaultValue = value
    }

    protected fun coerceInput(input: Any): T {
        // Agora 100% seguro com null
        if (input !is Number && input !is String && input !is CharSequence) {
            throw IllegalArgumentException(
                errorMessage ?: "Cannot coerce from ${input::class.simpleName} to ${type.name.lowercase()}. Expected number or string."
            )
        }

        val text = input.toString().trim()
        if (text.isEmpty()) {
            throw IllegalArgumentException(errorMessage ?: "Cannot coerce empty string to ${type.name.lowercase()}")
        }

        return try {
            when (type) {
                NumberSchemaType.INT -> text.toInt() as T
                NumberSchemaType.LONG -> text.toLong() as T
                NumberSchemaType.FLOAT -> text.toFloat() as T
                NumberSchemaType.DOUBLE -> text.toDouble() as T
                NumberSchemaType.BIGINT -> BigInteger(text) as T
                NumberSchemaType.BIGDECIMAL -> BigDecimal(text) as T
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                errorMessage ?: "Cannot parse '$text' as ${type.name.lowercase()}"
            )
        }
    }
}