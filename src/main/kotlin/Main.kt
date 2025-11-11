package studio.styx.sx

fun main() {
    val stringSchema = sx.coerce.string()
        .minLength(1)

    val arraySchema = sx.coerce.array<String>(

    )
}