package studio.styx.schemaEXtended.core.schemas;

import studio.styx.schemaEXtended.core.ObjectSchemaResult;
import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ArraySchema<T> extends Schema<List<T>> {
    private Schema<T> elementSchema;
    private Integer minLength;
    private Integer maxLength;
    private String minLengthError = "Array must have at least {min} elements";
    private String maxLengthError = "Array must have at most {max} elements";
    private String parseError = "The provided value is not an array";
    private String elementError = "One or more array elements are invalid";
    private List<T> defaultValue;
    private boolean unique = false;
    private String uniqueError = "Array must contain unique elements";
    private Function<T, Object> uniqueBy; // Para unicidade baseada em propriedade

    public ArraySchema(Schema<T> elementSchema) {
        this.elementSchema = elementSchema;
    }

    // Métodos de configuração
    public ArraySchema<T> minLength(int minLength) {
        this.minLength = minLength;
        this.minLengthError = this.minLengthError.replace("{min}", String.valueOf(minLength));
        return this;
    }

    public ArraySchema<T> maxLength(int maxLength) {
        this.maxLength = maxLength;
        this.maxLengthError = this.maxLengthError.replace("{max}", String.valueOf(maxLength));
        return this;
    }

    public ArraySchema<T> length(int exactLength) {
        this.minLength = exactLength;
        this.maxLength = exactLength;
        this.minLengthError = "Array must have exactly " + exactLength + " elements";
        this.maxLengthError = "Array must have exactly " + exactLength + " elements";
        return this;
    }

    public ArraySchema<T> minLengthError(String minLengthError) {
        this.minLengthError = minLengthError;
        return this;
    }

    public ArraySchema<T> maxLengthError(String maxLengthError) {
        this.maxLengthError = maxLengthError;
        return this;
    }

    public ArraySchema<T> parseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    public ArraySchema<T> elementError(String elementError) {
        this.elementError = elementError;
        return this;
    }

    public ArraySchema<T> defaultValue(List<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ArraySchema<T> unique() {
        this.unique = true;
        return this;
    }

    public ArraySchema<T> unique(String errorMessage) {
        this.unique = true;
        this.uniqueError = errorMessage;
        return this;
    }

    public ArraySchema<T> uniqueBy(Function<T, Object> uniqueBy) {
        this.unique = true;
        this.uniqueBy = uniqueBy;
        return this;
    }

    public ArraySchema<T> uniqueBy(Function<T, Object> uniqueBy, String errorMessage) {
        this.unique = true;
        this.uniqueBy = uniqueBy;
        this.uniqueError = errorMessage;
        return this;
    }

    public ArraySchema<T> nonEmpty() {
        this.minLength = 1;
        this.minLengthError = "Array must not be empty";
        return this;
    }

    @Override
    public ParseResult<List<T>> parse(Object value) {
        List<String> errors = new ArrayList<>();

        // Tratamento de null
        if (value == null) {
            if (defaultValue != null) {
                return ParseResult.success(new ArrayList<>(defaultValue));
            }
            if (this.isOptional()) {
                return ParseResult.success(new ArrayList<>());
            } else {
                return ParseResult.failure(List.of(parseError));
            }
        }

        // Coercion e conversão para lista
        List<Object> array = convertToList(value, errors);
        if (!errors.isEmpty()) {
            return ParseResult.failure(errors);
        }

        // Validações de array
        validateArraySize(array, errors);
        if (!errors.isEmpty()) {
            return ParseResult.failure(errors);
        }

        // Validação de elementos
        List<T> parsedElements = new ArrayList<>();
        List<String> elementErrors = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            Object element = array.get(i);
            ParseResult<T> elementResult = elementSchema.parse(element);

            if (elementResult.isSuccess()) {
                if (elementResult.getValue() != null) {
                    parsedElements.add(elementResult.getValue());
                }
            } else {
                String errorMsg = "Element at index " + i + ": " +
                        String.join(", ", elementResult.getErrors());
                elementErrors.add(errorMsg);
            }
        }

        // Validação de unicidade
        if (unique && !elementErrors.isEmpty()) {
            validateUniqueness(parsedElements, errors);
        }

        // Combinar erros
        if (!elementErrors.isEmpty()) {
            errors.add(elementError);
            errors.addAll(elementErrors);
        }

        return errors.isEmpty()
                ? ParseResult.success(parsedElements)
                : ParseResult.failure(errors);
    }

    @SuppressWarnings("unchecked")
    private List<Object> convertToList(Object value, List<String> errors) {
        if (value instanceof List) {
            return (List<Object>) value;
        }

        if (this.isCoerce()) {
            // Coercion de array para lista
            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                List<Object> list = new ArrayList<>();
                for (Object item : array) {
                    list.add(item);
                }
                return list;
            }
            // Coercion de string para lista (ex: "1,2,3" -> ["1", "2", "3"])
            else if (value instanceof String) {
                String str = (String) value;
                if (str.trim().startsWith("[") && str.trim().endsWith("]")) {
                    // Tenta parsear como JSON array simples
                    try {
                        String content = str.trim().substring(1, str.length() - 1);
                        String[] parts = content.split(",");
                        List<Object> list = new ArrayList<>();
                        for (String part : parts) {
                            list.add(part.trim());
                        }
                        return list;
                    } catch (Exception e) {
                        errors.add(parseError);
                        return null;
                    }
                } else {
                    // Split por vírgula
                    String[] parts = ((String) value).split(",");
                    List<Object> list = new ArrayList<>();
                    for (String part : parts) {
                        list.add(part.trim());
                    }
                    return list;
                }
            }
        }

        errors.add(parseError);
        return null;
    }

    private void validateArraySize(List<Object> array, List<String> errors) {
        if (array == null) return;

        int size = array.size();

        if (minLength != null && size < minLength) {
            errors.add(minLengthError);
        }

        if (maxLength != null && size > maxLength) {
            errors.add(maxLengthError);
        }
    }

    private void validateUniqueness(List<T> elements, List<String> errors) {
        if (elements == null || elements.isEmpty()) return;

        if (uniqueBy != null) {
            // Unicidade baseada em propriedade
            List<Object> keys = new ArrayList<>();
            for (T element : elements) {
                Object key = uniqueBy.apply(element);
                if (keys.contains(key)) {
                    errors.add(uniqueError);
                    return;
                }
                keys.add(key);
            }
        } else {
            // Unicidade padrão
            List<Object> seen = new ArrayList<>();
            for (T element : elements) {
                if (seen.contains(element)) {
                    errors.add(uniqueError);
                    return;
                }
                seen.add(element);
            }
        }
    }

    // Métodos auxiliares estáticos para criação rápida
    public static <T> ArraySchema<T> of(Schema<T> elementSchema) {
        return new ArraySchema<>(elementSchema);
    }

    public static ArraySchema<String> strings() {
        return new ArraySchema<>(new StringSchema());
    }

    public static ArraySchema<Number> numbers() {
        return new ArraySchema<>(new NumberSchema());
    }

    public static ArraySchema<ObjectSchemaResult> objects() {
        return new ArraySchema<>(new ObjectSchema());
    }
}