package studio.styx.schemaEXtended.core.schemas;

import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;

import java.util.List;

public class BooleanSchema extends Schema<Boolean> {
    private String parseError = "The provided value is not a boolean";
    private Boolean defaultValue;

    public BooleanSchema() {}
    public BooleanSchema(String errorMsg) { this.parseError = errorMsg; }

    public BooleanSchema defaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public BooleanSchema parseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    private Boolean coerceType(Object value) {
        if (value instanceof String) {
            return switch ((String) value) {
                case "true", "1", "yes", "on", "y" -> true;
                case "false", "0", "no", "off", "n" -> false;
                default -> null;
            };
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }

        return null;
    }

    @Override
    public ParseResult<Boolean> parse(Object value) {
        if (value == null) {
            if (defaultValue != null) {
                return ParseResult.success(defaultValue);
            }
            if (this.isOptional()) {
                return ParseResult.success(null);
            } else {
                return ParseResult.failure(List.of(parseError));
            }
        }

        if (this.isCoerce()) {
            Boolean coercedValue = coerceType(value);
            if (coercedValue != null) {
                return ParseResult.success(coercedValue);
            }
        }

        if (value instanceof Boolean) {
            return ParseResult.success((Boolean) value);
        } else {
            return ParseResult.failure(List.of(parseError));
        }
    }
}
