package studio.styx.schemaEXtended.core.batch;

import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchValidator {
    private final Map<String, Schema<?>> schemas = new HashMap<>();

    public BatchValidator add(String key, Schema<?> schema) {
        this.schemas.put(key, schema);
        return this;
    }

    public BatchResult validate() {
        Map<String, Object> successes = new HashMap<>();
        Map<String, List<String>> errors = new HashMap<>();

        schemas.forEach((key, schema) -> {
            // Tenta pegar o valor bindado no schema
            try {
                // parse() sem argumentos usa o valor do .bind()
                ParseResult<?> result = schema.parse();

                if (result.isSuccess()) {
                    successes.put(key, result.getValue());
                } else {
                    errors.put(key, result.getErrors());
                }
            } catch (IllegalStateException e) {
                // Caso o dev esqueça de dar bind/withSchema
                errors.put(key, List.of("No value bound to schema for validation"));
            }
        });

        return new BatchResult(successes, errors);
    }
}