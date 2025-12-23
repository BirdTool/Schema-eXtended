package studio.styx.schemaEXtended.core.schemas;

import studio.styx.schemaEXtended.core.ObjectSchemaResult;
import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectSchema extends Schema<ObjectSchemaResult> {
    private Map<String, Schema<?>> properties = new HashMap<>();
    private Map<String, Schema<?>> partialProperties = new HashMap<>();
    private boolean strict = false;
    private boolean allowUnknown = false;
    private String parseError = "The provided value is not a valid object or parseable string";

    public ObjectSchema() {}
    public ObjectSchema(String errorMsg) { this.parseError = errorMsg; }

    // --- Métodos de Configuração (Fluent Interface) ---
    @Override
    public ObjectSchema coerce() {
        super.coerce(); // Chama a lógica do pai (seta o boolean)
        return this;    // Retorna a instância tipada corretamente
    }

    @Override
    public ObjectSchema coerce(boolean coerce) {
        super.coerce(coerce);
        return this;
    }

    @Override
    public ObjectSchema optional() {
        super.optional();
        return this;
    }

    @Override
    public ObjectSchema optional(boolean optional) {
        super.optional(optional);
        return this;
    }

    public ObjectSchema addProperty(String name, Schema<?> schema) {
        this.properties.put(name, schema);
        return this;
    }

    public ObjectSchema addPartial(String name, Schema<?> schema) {
        this.partialProperties.put(name, schema);
        return this;
    }

    public ObjectSchema strict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public ObjectSchema strict() {
        this.strict = true;
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

    // --- Sobrecargas Específicas do Parse ---

    /**
     * Parse específico para Map
     */
    public ParseResult<ObjectSchemaResult> parse(Map<String, Object> value) {
        return parseInternal(value);
    }

    /**
     * Parse específico para String (tenta detectar JSON ou KV)
     */
    public ParseResult<ObjectSchemaResult> parse(String value) {
        return parse((Object) value); // Delega para o object que fará o roteamento no convertToMap
    }

    /**
     * Parse Genérico (Fallback e entrada principal)
     */
    @Override
    public ParseResult<ObjectSchemaResult> parse(Object value) {
        // 1. Tratamento de Null
        if (value == null) {
            if (this.isOptional()) {
                return ParseResult.success(new ObjectSchemaResult(new HashMap<>()));
            }
            return ParseResult.failure(List.of("Value cannot be null"));
        }

        // 2. Coercion e Conversão Inteligente
        Map<String, Object> objectMap = convertToMap(value);

        if (objectMap == null) {
            return ParseResult.failure(List.of(parseError));
        }

        // 3. Executa a validação no Map convertido
        return parseInternal(objectMap);
    }

    // --- Lógica Principal de Validação ---

    private ParseResult<ObjectSchemaResult> parseInternal(Map<String, Object> objectMap) {
        Map<String, Object> parsedValues = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        // Validação de Propriedades Obrigatórias
        for (Map.Entry<String, Schema<?>> entry : properties.entrySet()) {
            validateProperty(entry.getKey(), entry.getValue(), objectMap, parsedValues, fieldErrors, true);
        }

        // Validação de Propriedades Parciais
        for (Map.Entry<String, Schema<?>> entry : partialProperties.entrySet()) {
            validateProperty(entry.getKey(), entry.getValue(), objectMap, parsedValues, fieldErrors, false);
        }

        // Validação Strict (Campos desconhecidos)
        if (strict && !allowUnknown) {
            for (String key : objectMap.keySet()) {
                if (!properties.containsKey(key) && !partialProperties.containsKey(key)) {
                    fieldErrors.put(key, "Unknown property not allowed in strict mode");
                }
            }
        }

        if (fieldErrors.isEmpty()) {
            return ParseResult.success(new ObjectSchemaResult(parsedValues));
        } else {
            return ParseResult.failure(fieldErrors);
        }
    }

    private void validateProperty(String name, Schema<?> schema, Map<String, Object> source,
                                  Map<String, Object> target, Map<String, String> errors, boolean isRequired) {
        Object val = source.get(name);

        // Se o valor não existe no map, passamos null para o schema filho decidir (ele pode ter default value)
        ParseResult<?> result = schema.parse(val);

        if (result.isSuccess()) {
            if (result.getValue() != null) {
                target.put(name, result.getValue());
            }
        } else {
            // Se for opcional (partial) e o erro for apenas "não encontrado" ou null, ignoramos
            // Mas se for obrigatório, ou se o valor existia mas estava errado, registramos o erro
            if (isRequired || val != null) {
                List<String> errs = result.getErrors();
                errors.put(name, errs.isEmpty() ? "Invalid value" : errs.get(0));
            }
        }
    }

    // --- Lógica de Conversão ---

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object value) {
        // Caso 1: Já é um Map
        if (value instanceof Map) {
            return new HashMap<>((Map<String, Object>) value);
        }

        // Caso 2: Se Coerce estiver DESLIGADO, não tentamos converter Strings ou Objetos
        if (!this.isCoerce()) {
            return null;
        }

        // Caso 3: É uma String (Tenta JSON-like ou Key-Value)
        if (value instanceof String) {
            String strVal = ((String) value).trim();
            if (strVal.startsWith("{") && strVal.endsWith("}")) {
                return parseJsonLikeString(strVal);
            } else {
                return parseKeyValueString(strVal);
            }
        }

        // Caso 4: É um Objeto POJO (Java Bean) - Usamos Reflection
        return convertPojoToMap(value);
    }

    /**
     * Converte um POJO arbitrário para Map usando Reflection.
     * Pega todos os campos declarados (mesmo privados).
     */
    private Map<String, Object> convertPojoToMap(Object pojo) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = pojo.getClass();

        // Evita tentar converter primitivos ou wrappers como se fossem POJOs complexos
        if (isPrimitiveOrWrapper(clazz)) return null;

        try {
            // Itera sobre campos da classe e superclasses se necessário
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true); // Permite ler campos privados
                Object fieldValue = field.get(pojo);
                map.put(field.getName(), fieldValue);
            }
            return map;
        } catch (IllegalAccessException e) {
            // Logar erro se necessário
            return null;
        }
    }

    /**
     * Parser simples para strings estilo: "key=value, key2=value2"
     * Suporta separadores: , ; &
     */
    private Map<String, Object> parseKeyValueString(String text) {
        Map<String, Object> map = new HashMap<>();
        // Quebra por virgula, ponto-virgula ou &
        String[] pairs = text.split("[,;&]");

        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String val = kv[1].trim();
                map.put(key, val);
            }
        }
        return map.isEmpty() ? null : map;
    }

    /**
     * Parser rudimentar para JSON flat (ex: "{'a': 1, 'b': 'texto'}")
     * NOTA: Para JSONs complexos/aninhados, ideal é usar Jackson/Gson.
     */
    private Map<String, Object> parseJsonLikeString(String json) {
        Map<String, Object> map = new HashMap<>();
        // Remove chaves externas
        String content = json.substring(1, json.length() - 1);

        // Regex simplificado para pegar "chave": "valor" ou "chave": valor
        // Cuidado: Isso falha se o valor tiver vírgulas dentro.
        // Para robustez total, use uma lib de JSON.
        String[] entries = content.split(",");

        for (String entry : entries) {
            String[] kv = entry.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$|^'|'$", ""); // Remove aspas da chave
                String val = kv[1].trim().replaceAll("^\"|\"$|^'|'$", ""); // Remove aspas do valor
                map.put(key, val);
            }
        }
        return map;
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Double.class || clazz == Float.class ||
                clazz == Long.class || clazz == Integer.class ||
                clazz == Short.class || clazz == Character.class ||
                clazz == Byte.class || clazz == Boolean.class ||
                clazz == String.class;
    }
}