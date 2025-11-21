package studio.styx.schemaEXtended.core;

public class Schema<T> {
    private boolean coerce = false;
    private boolean opcional = false;

    public ParseResult<T> parse(Object obj) {
        return ParseResult.success((T) obj);
    }

    public Schema<T> coerce() {
        this.coerce = true;
        return this;
    }

    public Schema<T> coerce(boolean coerce) {
        this.coerce = coerce;
        return this;
    }

    public Schema<T> optional() {
        this.opcional = true;
        return this;
    }

    public Schema<T> optional(boolean optional) {
        this.opcional = optional;
        return this;
    }

    public boolean isCoerce() {
        return this.coerce;
    }

    public boolean isOptional() {
        return this.opcional;
    }
}