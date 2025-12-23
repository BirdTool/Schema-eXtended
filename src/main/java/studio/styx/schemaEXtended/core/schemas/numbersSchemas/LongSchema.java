package studio.styx.schemaEXtended.core.schemas.numbersSchemas;

import studio.styx.schemaEXtended.core.interfaces.NumberType;

public class LongSchema extends NumberSchema<Long> {
    public LongSchema() {
        super(NumberType.LONG);
    }
    public LongSchema(String errorMsg) {
        super(NumberType.LONG);
        super.parseError(errorMsg);
    }
}