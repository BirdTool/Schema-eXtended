package studio.styx.schemaEXtended.kotlin.core

import studio.styx.schemaEXtended.core.Schema
import studio.styx.schemaEXtended.core.batch.BatchResult
import studio.styx.schemaEXtended.core.batch.BatchValidator

class BatchBuilder {
    val validator = BatchValidator()

    // Permite a sintaxe: "key" to schema
    // MAS, como "to" cria um Pair em Kotlin, vamos usar uma função INFIX para ficar mais bonito e performático
    infix fun String.rules(schema: Schema<*>) {
        validator.add(this, schema)
    }

    // Alternativa usando o operador de atribuição customizado (opcional, mas legal)
    // "key"(schema)
    operator fun String.invoke(schema: Schema<*>) {
        validator.add(this, schema)
    }
}

// A função principal
fun validateBatch(block: BatchBuilder.() -> Unit): BatchResult {
    val builder = BatchBuilder()
    builder.block()
    return builder.validator.validate()
}