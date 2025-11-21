package studio.styx.schemaEXtended.core;

import java.util.List;
import java.util.Map;

public class ParseResult<T> {
    private final T value;
    private final Object errors; // Pode ser List<String> ou Map<String, String>
    private final boolean success;

    private ParseResult(T value, Object errors, boolean success) {
        this.value = value;
        this.errors = errors;
        this.success = success;
    }

    // Para erros simples (List<String>)
    public static <T> ParseResult<T> success(T value) {
        return new ParseResult<>(value, List.of(), true);
    }

    public static <T> ParseResult<T> failure(List<String> errors) {
        return new ParseResult<>(null, errors, false);
    }

    // Para erros complexos (Map<String, String>)
    public static <T> ParseResult<T> failure(Map<String, String> errors) {
        return new ParseResult<>(null, errors, false);
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public List<String> getErrors() {
        if (errors instanceof List) {
            return (List<String>) errors;
        }
        return List.of(); // Retorna lista vazia se for Map
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getFieldErrors() {
        if (errors instanceof Map) {
            return (Map<String, String>) errors;
        }
        return Map.of(); // Retorna map vazio se for List
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean hasErrors() {
        if (errors instanceof List) {
            return !((List<?>) errors).isEmpty();
        } else if (errors instanceof Map) {
            return !((Map<?, ?>) errors).isEmpty();
        }
        return false;
    }
}