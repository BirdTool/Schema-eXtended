package studio.styx.schemaEXtended.core.schemas;

import studio.styx.schemaEXtended.core.ParseResult;
import studio.styx.schemaEXtended.core.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringSchema extends Schema<String> {
    private String parseError = "The provided value is not a string";
    private Integer minLength;
    private Integer maxLength;
    private String minLengthError = "The text length is less than the required length";
    private String maxLengthError = "The text length is bigger than the required length";
    private String defaultValue;
    private boolean isEmail = false;
    private String emailError = "The provided email is not a valid email";
    private boolean isPhone = false;
    private String phoneError = "The provided phone is not a valid phone";
    private boolean isUrl = false;
    private String urlError = "The provided url is not a valid url";
    private String regex;
    private String regexError = "The provided value does not match the required regex";
    private boolean trim = false;
    private boolean toLowerCase = false;
    private boolean toUpperCase = false;

    // Métodos de configuração (fluent interface)
    public StringSchema parseError(String parseError) {
        this.parseError = parseError;
        return this;
    }

    public StringSchema minLength(int minLength) {
        this.minLength = minLength;
        return this;
    }

    public StringSchema maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public StringSchema minLengthError(String minLengthError) {
        this.minLengthError = minLengthError;
        return this;
    }

    public StringSchema maxLengthError(String maxLengthError) {
        this.maxLengthError = maxLengthError;
        return this;
    }

    public StringSchema defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public StringSchema email() {
        this.isEmail = true;
        return this;
    }

    public StringSchema email(String errorMessage) {
        this.isEmail = true;
        this.emailError = errorMessage;
        return this;
    }

    public StringSchema phone() {
        this.isPhone = true;
        return this;
    }

    public StringSchema phone(String errorMessage) {
        this.isPhone = true;
        this.phoneError = errorMessage;
        return this;
    }

    public StringSchema url() {
        this.isUrl = true;
        return this;
    }

    public StringSchema url(String errorMessage) {
        this.isUrl = true;
        this.urlError = errorMessage;
        return this;
    }

    public StringSchema regex(String regex) {
        this.regex = regex;
        return this;
    }

    public StringSchema regex(String regex, String errorMessage) {
        this.regex = regex;
        this.regexError = errorMessage;
        return this;
    }

    public StringSchema emailError(String emailError) {
        this.emailError = emailError;
        return this;
    }

    public StringSchema phoneError(String phoneError) {
        this.phoneError = phoneError;
        return this;
    }

    public StringSchema urlError(String urlError) {
        this.urlError = urlError;
        return this;
    }

    public StringSchema regexError(String regexError) {
        this.regexError = regexError;
        return this;
    }

    public StringSchema trim() {
        this.trim = true;
        return this;
    }

    public StringSchema toLowerCase() {
        this.toLowerCase = true;
        return this;
    }

    public StringSchema toUpperCase() {
        this.toUpperCase = true;
        return this;
    }

    @Override
    public ParseResult<String> parse(Object value) {
        List<String> errors = new ArrayList<>();

        // Tratamento de null
        if (value == null) {
            if (defaultValue != null) {
                return ParseResult.success(applyTransformations(defaultValue));
            }
            if (this.isOptional()) {
                return ParseResult.success(null);
            } else {
                return ParseResult.failure(List.of(parseError));
            }
        }

        // Coercion e conversão para string
        String stringValue = convertToString(value, errors);
        if (!errors.isEmpty()) {
            return ParseResult.failure(errors);
        }

        // Aplicar transformações
        stringValue = applyTransformations(stringValue);

        // Validações
        validateString(stringValue, errors);

        return errors.isEmpty()
                ? ParseResult.success(stringValue)
                : ParseResult.failure(errors);
    }

    private String convertToString(Object value, List<String> errors) {
        if (value instanceof String) {
            return (String) value;
        }

        if (this.isCoerce()) {
            // Coercion de outros tipos para string
            if (value instanceof Number || value instanceof Boolean || value instanceof Character) {
                return String.valueOf(value);
            } else {
                errors.add(parseError);
                return null;
            }
        }

        errors.add(parseError);
        return null;
    }

    private String applyTransformations(String value) {
        if (value == null) return null;

        String result = value;

        if (trim) {
            result = result.trim();
        }

        if (toLowerCase) {
            result = result.toLowerCase();
        }

        if (toUpperCase) {
            result = result.toUpperCase();
        }

        return result;
    }

    private void validateString(String value, List<String> errors) {
        if (value == null) return;

        // Validação de comprimento mínimo
        if (minLength != null && value.length() < minLength) {
            errors.add(minLengthError);
        }

        // Validação de comprimento máximo
        if (maxLength != null && value.length() > maxLength) {
            errors.add(maxLengthError);
        }

        // Validação de email
        if (isEmail && !isValidEmail(value)) {
            errors.add(emailError);
        }

        // Validação de telefone
        if (isPhone && !isValidPhone(value)) {
            errors.add(phoneError);
        }

        // Validação de URL
        if (isUrl && !isValidUrl(value)) {
            errors.add(urlError);
        }

        // Validação de regex
        if (regex != null && !isValidRegex(value, regex)) {
            errors.add(regexError);
        }
    }

    // Métodos de validação
    private boolean isValidEmail(String email) {
        // Regex simples para email - pode ser melhorado conforme necessidade
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Regex básica para números de telefone - adapte conforme necessidade
        // Aceita formatos internacionais e locais
        String phoneRegex = "^[\\+]?[0-9\\s\\-\\(\\)]{10,}$";
        return Pattern.compile(phoneRegex).matcher(phone.replaceAll("\\s", "")).matches();
    }

    private boolean isValidUrl(String url) {
        // Regex básica para URLs
        String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE).matcher(url).matches();
    }

    private boolean isValidRegex(String value, String regex) {
        try {
            return Pattern.compile(regex).matcher(value).matches();
        } catch (Exception e) {
            // Se o regex for inválido, considera como falha na validação
            return false;
        }
    }

    // Métodos auxiliares para validações específicas
    public StringSchema length(int exactLength) {
        this.minLength = exactLength;
        this.maxLength = exactLength;
        this.minLengthError = "The text length must be exactly " + exactLength + " characters";
        this.maxLengthError = "The text length must be exactly " + exactLength + " characters";
        return this;
    }

    public StringSchema length(int exactLength, String errorMessage) {
        this.minLength = exactLength;
        this.maxLength = exactLength;
        this.minLengthError = errorMessage;
        this.maxLengthError = errorMessage;
        return this;
    }

    public StringSchema nonEmpty() {
        this.minLength = 1;
        this.minLengthError = "The string must not be empty";
        return this;
    }

    public StringSchema nonEmpty(String errorMessage) {
        this.minLength = 1;
        this.minLengthError = errorMessage;
        return this;
    }
}