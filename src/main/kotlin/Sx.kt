package studio.styx.sx

import studio.styx.sx.core.*
import studio.styx.sx.types.NumberSchemaType
import studio.styx.sx.types.Schema

object sx {
    // === PRIMITIVOS ===
    fun int(errorMessage: String? = null) = NumberSchema<Int>(NumberSchemaType.INT, errorMessage, false)
    fun double(errorMessage: String? = null) = NumberSchema<Double>(NumberSchemaType.DOUBLE, errorMessage, false)
    fun float(errorMessage: String? = null) = NumberSchema<Float>(NumberSchemaType.FLOAT, errorMessage, false)
    fun long(errorMessage: String? = null) = NumberSchema<Long>(NumberSchemaType.LONG, errorMessage, false)
    fun string(errorMessage: String? = null) = StringSchema(errorMessage, false)
    fun boolean(errorMessage: String? = null) = BooleanSchema(errorMessage, false)

    // === ARRAYS ===
    fun <T> array(schema: Schema<T>? = null, errorMessage: String? = null) =
        ArraySchema(schema, errorMessage, coerce = false)

    // === OBJECT ===
    inline fun <reified T : Any> object_(
        errorMessage: String? = null,
        noinline constructor: ((Map<String, Any?>) -> T)? = null,
        crossinline block: ObjectSchemaBuilder<T>.() -> Unit
    ): ObjectSchema<T> {
        return ObjectSchemaBuilder(constructor, errorMessage).apply(block).build(coerce = false)
    }

    object coerce {
        fun int(errorMessage: String? = null) = NumberSchema<Int>(NumberSchemaType.INT, errorMessage, true)
        fun double(errorMessage: String? = null) = NumberSchema<Double>(NumberSchemaType.DOUBLE, errorMessage, true)
        fun float(errorMessage: String? = null) = NumberSchema<Float>(NumberSchemaType.FLOAT, errorMessage, true)
        fun long(errorMessage: String? = null) = NumberSchema<Long>(NumberSchemaType.LONG, errorMessage, true)
        fun string(errorMessage: String? = null) = StringSchema(errorMessage, true)
        fun boolean(errorMessage: String? = null) = BooleanSchema(errorMessage, true)

        fun <T> array(schema: Schema<T>? = null, errorMessage: String? = null) =
            ArraySchema(schema, errorMessage, coerce = true)

        inline fun <reified T : Any> object_(
            errorMessage: String? = null,
            noinline constructor: ((Map<String, Any?>) -> T)? = null,
            crossinline block: ObjectSchemaBuilder<T>.() -> Unit
        ): ObjectSchema<T> {
            return ObjectSchemaBuilder(constructor, errorMessage).apply(block).build(coerce = true)
        }
    }
}


class ObjectSchemaBuilder<T : Any>(
    private val constructor: ((Map<String, Any?>) -> T)?,
    private val errorMessage: String?
) {
    private val shape = mutableMapOf<String, Schema<*>>()

    infix fun String.to(schema: Schema<*>) {
        shape[this] = schema
    }

    operator fun String.invoke(schema: Schema<*>) = to(schema)

    fun build(coerce: Boolean = false): ObjectSchema<T> =
        ObjectSchema(shape.toMap(), constructor, errorMessage, coerce) // ORDEM CORRETA!
}