package studio.styx.schemaEXtended.core.interfaces;

import java.util.Map;

public class ObjectParseResult {
    private final Map<String, Object> parsedValues;
    private final Map<String, String> errors;

    public ObjectParseResult(Map<String, Object> parsedValues, Map<String, String> errors) {
        this.parsedValues = parsedValues;
        this.errors = errors;
    }

    public Map<String, Object> getParsedValues() {
        return parsedValues;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public Object getValue(String key) {
        return parsedValues.get(key);
    }

    public String getError(String key) {
        return errors.get(key);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}