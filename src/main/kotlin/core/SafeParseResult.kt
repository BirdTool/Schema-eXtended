package studio.styx.sx.core

data class SafeParseResult<out T>(
    val success: Boolean,
    val value: T? = null,
    val errors: List<String> = emptyList()
) {
    companion object {
        fun <T> success(value: T): SafeParseResult<Boolean> =
            SafeParseResult(success = true, value = value, errors = emptyList())

        fun <T> failure(vararg errors: String): SafeParseResult<T> =
            SafeParseResult(success = false, value = null, errors = errors.toList())

        fun <T> failure(errors: List<String>): SafeParseResult<T> =
            SafeParseResult(success = false, value = null, errors = errors)
    }

    inline fun onSuccess(block: (T) -> Unit): SafeParseResult<T> = apply {
        if (success) value?.let(block)
    }

    inline fun onFailure(block: (List<String>) -> Unit): SafeParseResult<T> = apply {
        if (!success) block(errors)
    }
}