package studio.styx.schemaEXtended.core.schemas;

import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;
import studio.styx.schemaEXtended.core.interfaces.NumberType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NumberSchema extends Schema<Number> {
    private NumberType type;
    private Double min;
    private Double max;
    private String minError = "The provided number is less than the minimum required";
    private String maxError = "The provided number is bigger than the maximum required";
    private String parseError = "The provided value is not a number";
    private String typeError = "The provided number does not match the required type";
    private Double defaultValue;
    private boolean integerOnly = false;

    public NumberSchema() {
        this.type = NumberType.DOUBLE;
    }

    public NumberSchema(NumberType type) {
        this.type = type;
        this.integerOnly = type == NumberType.INT || type == NumberType.LONG || type == NumberType.BIGINT;
    }

    // Métodos de configuração
    public NumberSchema min(double min) {
        this.min = min;
        return this;
    }

    public NumberSchema max(double max) {
        this.max = max;
        return this;
    }

    public NumberSchema min(int min) {
        this.min = (double) min;
        return this;
    }

    public NumberSchema max(int max) {
        this.max = (double) max;
        return this;
    }

    public NumberSchema minError(String minError) {
        this.minError = minError;
        return this;
    }

    public NumberSchema maxError(String maxError) {
        this.maxError = maxError;
        return this;
    }

    public NumberSchema parseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    public NumberSchema typeError(String typeError) {
        this.typeError = typeError;
        return this;
    }

    public NumberSchema defaultValue(Number defaultValue) {
        this.defaultValue = defaultValue.doubleValue();
        return this;
    }

    public NumberSchema integer() {
        this.integerOnly = true;
        if (this.type == NumberType.DOUBLE || this.type == NumberType.FLOAT) {
            this.type = NumberType.INT;
        }
        return this;
    }

    @Override
    public ParseResult<Number> parse(Object value) {
        List<String> errors = new ArrayList<>();

        // Tratamento de null
        if (value == null) {
            if (defaultValue != null) {
                return ParseResult.success(convertToType(defaultValue));
            }
            if (this.isOptional()) {
                return ParseResult.success(null);
            } else {
                return ParseResult.failure(List.of(parseError));
            }
        }

        // Coercion de string para número
        if (this.isCoerce() && value instanceof String) {
            try {
                value = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return ParseResult.failure(List.of(parseError));
            }
        }

        // Conversão para número
        Number number = convertToNumber(value, errors);
        if (!errors.isEmpty()) {
            return ParseResult.failure(errors);
        }

        // Validações
        validateNumber(number, errors);

        return errors.isEmpty()
                ? ParseResult.success(convertToType(number))
                : ParseResult.failure(errors);
    }

    private Number convertToNumber(Object value, List<String> errors) {
        if (value instanceof Number) {
            return (Number) value;
        }

        if (this.isCoerce()) {
            try {
                if (value instanceof String) {
                    return Double.parseDouble((String) value);
                } else if (value instanceof Boolean) {
                    return (Boolean) value ? 1 : 0;
                }
            } catch (NumberFormatException e) {
                errors.add(parseError);
                return null;
            }
        }

        errors.add(parseError);
        return null;
    }

    private void validateNumber(Number number, List<String> errors) {
        if (number == null) return;

        double value = number.doubleValue();

        // Validação de mínimo
        if (min != null && value < min) {
            errors.add(minError);
        }

        // Validação de máximo
        if (max != null && value > max) {
            errors.add(maxError);
        }

        // Validação de tipo inteiro
        if (integerOnly && value % 1 != 0) {
            errors.add(typeError);
        }
    }

    private Number convertToType(Number number) {
        if (number == null) return null;

        return switch (this.type) {
            case INT -> number.intValue();
            case LONG -> number.longValue();
            case FLOAT -> number.floatValue();
            case BIGINT -> BigInteger.valueOf(number.longValue());
            case BIGDECIMAL -> BigDecimal.valueOf(number.doubleValue());
            default -> number.doubleValue();
        };
    }
}