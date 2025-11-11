package studio.styx.sx.types

import studio.styx.sx.core.SafeParseResult

interface Schema<T> {
    fun safeParse(input: Any?): SafeParseResult<T>
    fun parse(input: Any?): T = safeParse(input).let {
        if (!it.success) throw IllegalArgumentException(it.errors.joinToString(" | "))
        it.value!!
    }

    fun parseOptional(input: Any?): T? = safeParse(input).value
}