package studio.styx.schemaEXtended.core.schemas;

import studio.styx.schemaEXtended.core.ObjectSchemaResult;
import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectSchema extends Schema<ObjectSchemaResult> {
    private Map<String, Schema<?>> properties = new HashMap<>();
    private Map<String, Schema<?>> partialProperties = new HashMap<>();
    private boolean strict = false;
    private boolean allowUnknown = false;
    private String parseError = "The provided value is not an object";

    // Métodos de configuração (mantidos iguais)
    public ObjectSchema addProperty(String name, Schema<?> schema) {
        this.properties.put(name, schema);
        return this;
    }

    public ObjectSchema addPartial(String name, Schema<?> schema) {
        this.partialProperties.put(name, schema);
        return this;
    }

    public ObjectSchema setProperties(Map<String, Schema<?>> properties) {
        this.properties = new HashMap<>(properties);
        return this;
    }

    public ObjectSchema removeProperty(String name) {
        this.properties.remove(name);
        this.partialProperties.remove(name);
        return this;
    }

    public ObjectSchema strict() {
        this.strict = true;
        return this;
    }

    public ObjectSchema strict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public ObjectSchema allowUnknown() {
        this.allowUnknown = true;
        return this;
    }

    public ObjectSchema allowUnknown(boolean allowUnknown) {
        this.allowUnknown = allowUnknown;
        return this;
    }

    public ObjectSchema parseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    public Map<String, Schema<?>> getProperties() {
        return new HashMap<>(properties);
    }

    public Map<String, Schema<?>> getPartialProperties() {
        return new HashMap<>(partialProperties);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name) || partialProperties.containsKey(name);
    }

    public Schema<?> getProperty(String name) {
        Schema<?> schema = properties.get(name);
        if (schema == null) {
            schema = partialProperties.get(name);
        }
        return schema;
    }

    @Override
    public ParseResult<ObjectSchemaResult> parse(Object value) {
        // Tratamento de null
        if (value == null) {
            if (this.isOptional()) {
                return ParseResult.success(new ObjectSchemaResult(new HashMap<>()));
            } else {
                return ParseResult.failure(List.of(parseError));
            }
        }

        // Coercion - tenta converter para Map se possível
        Map<String, Object> objectMap = convertToMap(value);
        if (objectMap == null) {
            return ParseResult.failure(List.of(parseError));
        }

        Map<String, Object> parsedValues = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        // Processar propriedades obrigatórias
        for (Map.Entry<String, Schema<?>> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            Schema<?> propertySchema = entry.getValue();

            Object propertyValue = objectMap.get(propertyName);
            ParseResult<?> propertyResult = propertySchema.parse(propertyValue);

            if (propertyResult.isSuccess()) {
                if (propertyResult.getValue() != null) {
                    parsedValues.put(propertyName, propertyResult.getValue());
                }
            } else {
                List<String> errors = propertyResult.getErrors();
                if (errors.isEmpty()) {
                    fieldErrors.put(propertyName, "Validation failed");
                } else {
                    errors.forEach(e -> fieldErrors.put(propertyName, e));
                }
            }
        }

        // Processar propriedades parciais (opcionais)
        for (Map.Entry<String, Schema<?>> entry : partialProperties.entrySet()) {
            String propertyName = entry.getKey();
            Schema<?> propertySchema = entry.getValue();

            Object propertyValue = objectMap.get(propertyName);
            ParseResult<?> propertyResult = propertySchema.parse(propertyValue);

            if (propertyResult.isSuccess() && propertyResult.getValue() != null) {
                parsedValues.put(propertyName, propertyResult.getValue());
            }
            // Não adiciona erro para propriedades parciais que falham
        }

        // Validar propriedades desconhecidas no modo strict
        if (strict && !allowUnknown) {
            for (String key : objectMap.keySet()) {
                if (!properties.containsKey(key) && !partialProperties.containsKey(key)) {
                    fieldErrors.put(key, "Unknown property");
                }
            }
        }

        // Se não há erros, retorna sucesso
        if (fieldErrors.isEmpty()) {
            return ParseResult.success(new ObjectSchemaResult(parsedValues));
        } else {
            return ParseResult.failure(fieldErrors);
        }
    }

    private Map<String, Object> convertToMap(Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return new HashMap<>(map);
        }

        if (this.isCoerce()) {
            // Tentativa de coercion - pode ser extendido para outros tipos
            // Por exemplo, converter objeto POJO para Map usando reflection
            // Por enquanto, só suportamos Map
            return null;
        }

        return null;
    }

    // Métodos auxiliares para criação de schemas complexos
    public static ObjectSchema create() {
        return new ObjectSchema();
    }

    public ObjectSchema merge(ObjectSchema other) {
        ObjectSchema merged = new ObjectSchema();
        merged.properties.putAll(this.properties);
        merged.properties.putAll(other.properties);
        merged.partialProperties.putAll(this.partialProperties);
        merged.partialProperties.putAll(other.partialProperties);
        merged.strict = this.strict || other.strict;
        merged.allowUnknown = this.allowUnknown && other.allowUnknown;
        return merged;
    }
}