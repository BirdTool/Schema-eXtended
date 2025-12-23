package studio.styx.schemaEXtended.core.schemas.numbersSchemas;

import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;
import studio.styx.schemaEXtended.core.interfaces.NumberType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

// ADICIONADO <T extends Number> AQUI
public class NumberSchema<T extends Number> extends Schema<T> {

    private NumberType type;
    private Double min;
    private Double max;
    private String minError = "The provided number is less than the minimum required";
    private String maxError = "The provided number is bigger than the maximum required";
    private String parseError = "The provided value is not a number";
    private String typeError = "The provided number does not match the required type";
    private Number defaultValue;
    private boolean integerOnly = false;

    @Override
    public NumberSchema<T> coerce() {
        super.coerce();
        return this;
    }

    public NumberSchema(String parseError) {
        this.type = NumberType.DOUBLE;
        this.parseError(parseError);
    }

    public NumberSchema() {
        this.type = NumberType.DOUBLE;
    }

    public NumberSchema(NumberType type) {
        this.type = type;
        this.integerOnly = type == NumberType.INT || type == NumberType.LONG || type == NumberType.BIGINT;
    }

    public NumberSchema<T> min(double min) {
        this.min = min;
        return this;
    }

    public NumberSchema<T> max(double max) {
        this.max = max;
        return this;
    }

    public NumberSchema<T> min(double min, String error) {
        this.min = min;
        this.minError = error;
        return this;
    }

    public NumberSchema<T> max(double max, String error) {
        this.max = max;
        this.maxError = error;
        return this;
    }

    public NumberSchema<T> min(Integer min) {
        this.min = min.doubleValue();
        return this;
    }

    public NumberSchema<T> max(Integer max) {
        this.max = max.doubleValue();
        return this;
    }

    public NumberSchema<T> minError(String minError) {
        this.minError = minError;
        return this;
    }

    public NumberSchema<T> maxError(String maxError) {
        this.maxError = maxError;
        return this;
    }

    public NumberSchema<T> parseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    public NumberSchema<T> typeError(String typeError) {
        this.typeError = typeError;
        return this;
    }

    public NumberSchema<T> defaultValue(Number defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public NumberSchema<T> integer() {
        this.integerOnly = true;
        if (this.type == NumberType.DOUBLE || this.type == NumberType.FLOAT) {
            this.type = NumberType.INT;
        }
        return this;
    }

    @Override
    public ParseResult<T> parse(Object value) {
        List<String> errors = new ArrayList<>();

        // Tratamento de null
        if (value == null) {
            if (defaultValue != null) {
                // Cast seguro pois convertToType garante o retorno T
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

        if (min != null && value < min) errors.add(minError);
        if (max != null && value > max) errors.add(maxError);
        if (integerOnly && value % 1 != 0) errors.add(typeError);
    }

    @SuppressWarnings("unchecked")
    private T convertToType(Number number) {
        if (number == null) return null;

        Number result = switch (this.type) {
            case INT -> number.intValue();
            case LONG -> number.longValue();
            case FLOAT -> number.floatValue();
            case BIGINT -> BigInteger.valueOf(number.longValue());
            case BIGDECIMAL -> BigDecimal.valueOf(number.doubleValue());
            default -> number.doubleValue();
        };

        return (T) result;
    }
}