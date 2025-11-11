package studio.styx.sx

import studio.styx.sx.core.ArraySchema
import studio.styx.sx.core.BooleanSchema
import studio.styx.sx.core.NumberSchema
import studio.styx.sx.core.StringSchema
import studio.styx.sx.types.NumberSchemaType

object sx {
    fun int(errorMessage: String? = null) = NumberSchema<Int>(NumberSchemaType.INT, errorMessage, false)
    fun double(errorMessage: String? = null) = NumberSchema<Double>(NumberSchemaType.DOUBLE, errorMessage, false)
    fun float(errorMessage: String? = null) = NumberSchema<Float>(NumberSchemaType.FLOAT, errorMessage, false)
    fun long(errorMessage: String? = null) = NumberSchema<Long>(NumberSchemaType.LONG, errorMessage, false)
    fun string(errorMessage: String? = null) = StringSchema(errorMessage, false)
    fun boolean(errorMessage: String? = null) = BooleanSchema(errorMessage, false)
    fun <T> array(itemSchema: String? = null, errorMessage: String? = null) = ArraySchema<T>(itemSchema, errorMessage, false)

    object coerce {
        fun int(errorMessage: String? = null) = NumberSchema<Int>(NumberSchemaType.INT, errorMessage, true)
        fun double(errorMessage: String? = null) = NumberSchema<Double>(NumberSchemaType.DOUBLE, errorMessage, true)
        fun float(errorMessage: String? = null) = NumberSchema<Float>(NumberSchemaType.FLOAT, errorMessage, true)
        fun long(errorMessage: String? = null) = NumberSchema<Long>(NumberSchemaType.LONG, errorMessage, true)
        fun string(errorMessage: String? = null) = StringSchema(errorMessage, true)
        fun boolean(errorMessage: String? = null) = BooleanSchema(errorMessage, true)
        fun <T> array(itemSchema: String? = null, errorMessage: String? = null) = ArraySchema<T>(itemSchema, errorMessage, true)
    }
}
