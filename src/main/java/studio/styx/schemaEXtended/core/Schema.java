package studio.styx.schemaEXtended.core;

import studio.styx.schemaEXtended.core.exceptions.SchemaIllegalArgumentException;

public abstract class Schema<T> {
    private boolean coerce = false;
    private boolean opcional = false;

    public abstract ParseResult<T> parse(Object obj);

    public T parseOrThrow(Object value) {
        ParseResult<T> result = parse(value);

        if (!result.isSuccess()) {
            throw new SchemaIllegalArgumentException(result, value);
        }

        return result.getValue();
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