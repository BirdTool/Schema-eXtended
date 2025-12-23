package studio.styx.schemaEXtended.core.batch;

import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.exceptions.SchemaIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchResult {
    private final Map<String, Object> values; // Valores processados com sucesso ou null
    private final Map<String, List<String>> errors; // Mapa de erros

    public BatchResult(Map<String, Object> values, Map<String, List<String>> errors) {
        this.values = values;
        this.errors = errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    /**
     * Retorna os valores se tudo estiver OK.
     * Se houver erros, cria um ParseResult de falha e lança SchemaIllegalArgumentException.
     */
    public Map<String, Object> getOrThrow() {
        if (hasErrors()) {
            // 1. Achatar o mapa de erros em uma lista de strings
            // Formato: "campo: erro"
            List<String> flattenedErrors = new ArrayList<>();

            errors.forEach((key, errList) ->
                    errList.forEach(errorMsg ->
                            flattenedErrors.add("[" + key + "] " + errorMsg)
                    )
            );

            ParseResult<Map<String, Object>> failedResult = ParseResult.failure(flattenedErrors);

            throw new SchemaIllegalArgumentException(failedResult, values);
        }

        return values;
    }
}