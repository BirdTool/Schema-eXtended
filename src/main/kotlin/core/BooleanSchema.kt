package studio.styx.sx.core

import studio.styx.sx.types.Schema

class BooleanSchema(
    private val errorMessage: String? = null,
    private val coerce: Boolean = false,
) : Schema<Boolean> {
    private var defaultValue: Boolean? = null
    private var isOptional: Boolean = false
    private var mustBeTrue: Boolean? = null  // null = qualquer, true = só true, false = só false
    private var trueErrorMessage: String? = null
    private var falseErrorMessage: String? = null

    override fun safeParse(input: Any?): SafeParseResult<Boolean> {
        val errors = mutableListOf<String>()
        var parsed: Boolean? = null

        // 1. Null / vazio / optional / default
        if (input == null || input == "") {
            if (defaultValue != null) {
                parsed = defaultValue
            } else if (isOptional) {
                return SafeParseResult.success(null)
            } else {
                errors.add(errorMessage ?: "Value is required")
                return SafeParseResult.failure(errors)
            }
        } else {
            // 2. Coerce ou strict
            parsed = if (coerce) {
                when (input) {
                    is Boolean -> input
                    is Number -> input.toDouble() == 1.0
                    is String -> when (input.trim().lowercase()) {
                        "true", "1", "yes", "on", "t" -> true
                        "false", "0", "no", "off", "f" -> false
                        else -> null
                    }
                    else -> null
                }
            } else {
                input as? Boolean
            }

            if (parsed == null) {
                errors.add(errorMessage ?: "Expected a boolean, got ${input::class.simpleName}")
                return SafeParseResult.failure(errors)
            }
        }

        // 3. Refinamentos .true() / .false()
        val value = parsed!!
        mustBeTrue?.let { expected ->
            if (value != expected) {
                errors.add(
                    if (expected) trueErrorMessage ?: "Must be true"
                    else falseErrorMessage ?: "Must be false"
                )
            }
        }

        return if (errors.isEmpty()) {
            SafeParseResult.success(value)
        } else {
            SafeParseResult.failure(errors)
        }
    }

    // parse / parseOptional (usando safeParse)
    override fun parse(input: Any?): Boolean = safeParse(input).let { r ->
        if (!r.success) throw IllegalArgumentException(r.errors.joinToString(" | "))
        r.value!!
    }

    override fun parseOptional(input: Any?): Boolean? = safeParse(input).value

    // Builders
    fun default(value: Boolean) = apply { defaultValue = value }
    fun optional() = apply { isOptional = true }

    fun isTrue(errorMessage: String? = null) = apply {
        mustBeTrue = true
        trueErrorMessage = errorMessage
    }

    fun isFalse(errorMessage: String? = null) = apply {
        mustBeTrue = false
        falseErrorMessage = errorMessage
    }
}