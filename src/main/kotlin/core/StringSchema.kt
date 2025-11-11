package studio.styx.sx.core

import studio.styx.sx.types.Schema

@Suppress("UNCHECKED_CAST")
open class StringSchema(
    internal open val errorMessage: String? = null,
    internal open val coerce: Boolean = false
) : Schema<String> {
    private var minLength: Int? = null
    private var maxLength: Int? = null
    private var defaultValue: String? = null
    private var minLengthErrorMessage: String? = null
    private var maxLengthErrorMessage: String? = null
    private var pattern: Regex? = null
    private var patternErrorMessage: String? = null

    override fun safeParse(input: Any?): SafeParseResult<String> {
        val errors = mutableListOf<String>()
        var parsedValue: String? = null

        // 1. Trata null/vazio/default
        val trimmedInput = input?.toString()?.trim()
        if (input == null || input == "" || trimmedInput.isNullOrEmpty()) {
            if (defaultValue != null) {
                parsedValue = defaultValue
            } else {
                errors.add(errorMessage ?: "Value is required")
                return SafeParseResult.failure(errors)
            }
        } else {
            // 2. Tipo + coerce
            parsedValue = if (coerce) {
                trimmedInput
            } else {
                (input as? String)?.trim() ?: run {
                    errors.add(errorMessage ?: "Expected a string, got ${input::class.simpleName}")
                    return SafeParseResult.failure(errors)
                }
            }
        }

        // 3. Validações (agora com parsedValue garantido non-null)
        val value = parsedValue!!

        minLength?.let { min ->
            if (value.length < min) {
                errors.add(minLengthErrorMessage ?: "String too short: ${value.length} < $min")
            }
        }

        maxLength?.let { max ->
            if (value.length > max) {
                errors.add(maxLengthErrorMessage ?: "String too long: ${value.length} > $max")
            }
        }

        pattern?.let { regex ->
            if (!value.matches(regex)) {
                errors.add(patternErrorMessage ?: "Does not match pattern")
            }
        }

        return (if (errors.isEmpty()) {
            SafeParseResult.success(value)
        } else {
            SafeParseResult.failure(errors)
        }) as SafeParseResult<String>
    }

    // parse e parseOptional usando safeParse
    override fun parse(input: Any?): String = safeParse(input).let { result ->
        if (!result.success) throw IllegalArgumentException(result.errors.joinToString(" | "))
        result.value!!
    }

    override fun parseOptional(input: Any?): String? = safeParse(input).value

    fun minLength(length: Int, errorMessage: String? = null) = apply {
        minLength = length
        minLengthErrorMessage = errorMessage
    }

    fun maxLength(length: Int, errorMessage: String? = null) = apply {
        maxLength = length
        maxLengthErrorMessage = errorMessage
    }

    fun default(value: String) = apply { defaultValue = value }

    fun matches(regex: Regex, errorMessage: String? = null) = apply {
        pattern = regex
        patternErrorMessage = errorMessage
    }

    // ===== PRESETS PRONTOS =====

    fun email(errorMessage: String? = null) = matches(
        regex = Regex("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"),
        errorMessage = errorMessage ?: "Invalid email address"
    )

    fun uuid(errorMessage: String? = null) = matches(
        regex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"),
        errorMessage = errorMessage ?: "Invalid UUID format"
    )

    fun url(errorMessage: String? = null) = matches(
        regex = Regex("^(https?://)?[\\w.-]+(\\.[\\w.-]+)+[\\w.,@?^=%&:/~+#-]*$"),
        errorMessage = errorMessage ?: "Invalid URL"
    )

    fun phone(errorMessage: String? = null) = matches(
        // Aceita TODOS esses formatos brasileiros e internacionais:
        // +55 11 99999-9999 | +5511999999999 | (11) 99999-9999 | 11999999999 | 11 9999-9999 | 9999-9999
        regex = Regex("^(\\+?\\d{1,4}?[ \\-\\(\\)]?)?(\\d{2,5}[ \\-\\)]?)?(\\d{4,5}[- ]?\\d{4})$"),
        errorMessage = errorMessage ?: "Invalid phone number"
    )
}