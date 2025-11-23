package studio.styx.schemaEXtended.core;

import java.util.Map;

import java.util.HashMap;

public class ObjectSchemaResult {
    private final Map<String, Object> values;

    public ObjectSchemaResult(Map<String, Object> values) {
        this.values = new HashMap<>(values);
    }

    // Método principal com tipagem
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) values.get(key);
    }

    // Métodos auxiliares para tipos comuns
    public String getString(String key) {
        return get(key);
    }

    public Integer getInteger(String key) {
        return get(key);
    }

    public Double getDouble(String key) {
        return get(key);
    }

    public Boolean getBoolean(String key) {
        return get(key);
    }

    public Long getLong(String key) {
        return get(key);
    }

    // Método com valor padrão
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        T value = (T) values.get(key);
        return value != null ? value : defaultValue;
    }

    // Verificar se contém uma chave
    public boolean has(String key) {
        return values.containsKey(key);
    }

    // Obter o map completo
    public Map<String, Object> toMap() {
        return new HashMap<>(values);
    }

    // Tamanho
    public int size() {
        return values.size();
    }

    // Verificar se está vazio
    public boolean isEmpty() {
        return values.isEmpty();
    }
}