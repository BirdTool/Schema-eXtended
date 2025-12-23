package studio.styx.schemaEXtended.kotlin.core.extensions

import studio.styx.schemaEXtended.core.Schema
import studio.styx.schemaEXtended.core.schemas.BooleanSchema
import studio.styx.schemaEXtended.core.schemas.ObjectSchema
import studio.styx.schemaEXtended.core.schemas.StringSchema
import studio.styx.schemaEXtended.core.schemas.numbersSchemas.*
import java.math.BigDecimal

// --- String ---
fun String.withSchema(): StringSchema {
    val schema = StringSchema()
    schema.bind(this)
    return schema
}

// --- Inteiros ---
fun Int.withSchema(): IntegerSchema {
    val schema = IntegerSchema()
    schema.bind(this)
    return schema
}

// --- Doubles ---
fun Double.withSchema(): DoubleSchema {
    val schema = DoubleSchema()
    schema.bind(this)
    return schema
}

// --- Longs ---
fun Long.withSchema(): LongSchema {
    val schema = LongSchema()
    schema.bind(this)
    return schema
}

// --- Float ---
fun Float.withSchema(): FloatSchema {
    val schema = FloatSchema()
    schema.bind(this)
    return schema
}

// --- BigDecimal ---
fun BigDecimal.withSchema(): BigDecimalSchema {
    val schema = BigDecimalSchema()
    schema.bind(this)
    return schema
}

// --- Map (para ObjectSchema) ---
fun Map<String, Any>.withSchema(): ObjectSchema {
    val schema = ObjectSchema()
    schema.bind(this)
    return schema
}

fun Any.asStringSchema(): StringSchema {
    val schema = StringSchema()
    schema.bind(this)
    return schema.coerce() as StringSchema // Força coerce pois Any não é necessariamente String
}

// --- String para Integer ---
fun String.asIntSchema(): IntegerSchema {
    val schema = IntegerSchema()
    schema.bind(this)      // Amarra o valor "70"
    schema.coerce(true)    // ATIVA A CONVERSÃO AUTOMATICAMENTE
    return schema
}

// --- String para Double ---
fun String.asDoubleSchema(): DoubleSchema {
    val schema = DoubleSchema()
    schema.bind(this)
    schema.coerce(true)
    return schema
}

// --- String para Long ---
fun String.asLongSchema(): LongSchema {
    val schema = LongSchema()
    schema.bind(this)
    schema.coerce(true)
    return schema
}

// --- String para BigDecimal ---
fun String.asBigDecimalSchema(): BigDecimalSchema {
    val schema = BigDecimalSchema()
    schema.bind(this)
    schema.coerce(true)
    return schema
}

// --- String para Boolean (Útil para "true", "FALSE", "1") ---
fun String.asBooleanSchema(): BooleanSchema {
    val schema = BooleanSchema()
    schema.bind(this)
    schema.coerce(true)
    return schema
}

inline fun <reified S : Schema<T>, T> T.validate(schemaFactory: () -> S, config: S.() -> Unit): S {
    val schema = schemaFactory()
    schema.bind(this)
    schema.config()
    return schema
}
