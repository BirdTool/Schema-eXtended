package studio.styx.schemaEXtended.core.exceptions;

import studio.styx.schemaEXtended.core.ParseResult;

import java.util.List;
import java.util.Map;

public class SchemaIllegalArgumentException extends IllegalArgumentException {
    private final transient ParseResult<?> parseResult;
    private final Object originalValue;

    public SchemaIllegalArgumentException(String message, ParseResult<?> parseResult, Object originalValue) {
        super(message);
        this.parseResult = parseResult;
        this.originalValue = originalValue;
    }

    public SchemaIllegalArgumentException(ParseResult<?> parseResult, Object originalValue) {
        this("Schema validation failed", parseResult, originalValue);
    }

    // Getters para acessar os dados do parse
    public ParseResult<?> getParseResult() {
        return parseResult;
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public List<String> getErrors() {
        return parseResult.getErrors();
    }

    public Map<String, String> getFieldErrors() {
        return parseResult.getFieldErrors();
    }

    public boolean hasFieldErrors() {
        return !parseResult.getFieldErrors().isEmpty();
    }

    public boolean hasSimpleErrors() {
        return !parseResult.getErrors().isEmpty();
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());

        if (hasFieldErrors()) {
            sb.append("\nField errors:");
            getFieldErrors().forEach((field, error) -> {
                sb.append("\n  - ").append(field).append(": ").append(error);
            });
        }

        if (hasSimpleErrors()) {
            sb.append("\nErrors:");
            for (String error : getErrors()) {
                sb.append("\n  - ").append(error);
            }
        }

        sb.append("\nOriginal value: ").append(originalValue);

        return sb.toString();
    }
}