package studio.styx.sx.core

import studio.styx.sx.types.Schema

class OptionalSchema<T : Any>(
    private val schema: Schema<T>,
    private val defaultValue: T? = null
) : Schema<T?> {

    override fun safeParse(input: Any?): SafeParseResult<T?> {
        if (input == null || input == "" || (input is String && input.trim().isEmpty())) {
            return if (defaultValue != null) {
                SafeParseResult.success(defaultValue)
            } else {
                SafeParseResult.success(null)
            }
        }

        val result = schema.safeParse(input)
        return if (result.success) {
            SafeParseResult.success(result.value)
        } else {
            SafeParseResult.failure(result.errors)
        }
    }

    override fun parse(input: Any?): T? =
        safeParse(input).let { r ->
            if (!r.success && defaultValue == null) {
                throw IllegalArgumentException(r.errors.joinToString(" | "))
            }
            r.value
        }

    override fun parseOptional(input: Any?): T? = safeParse(input).value
}

fun <T : Any> Schema<T>.optional(defaultValue: T? = null): Schema<T?> =
    OptionalSchema(this, defaultValue)

// Para nullable (aceita null, mas não undefined)
fun <T : Any> Schema<T>.nullable(defaultValue: T? = null): Schema<T?> =
    OptionalSchema(this, defaultValue)