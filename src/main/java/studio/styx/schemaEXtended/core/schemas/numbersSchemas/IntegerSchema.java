package studio.styx.schemaEXtended.core.schemas.numbersSchemas;

import studio.styx.schemaEXtended.core.interfaces.NumberType;

public class IntegerSchema extends NumberSchema<Integer> {
    public IntegerSchema() {
        super(NumberType.INT);
    }
    public IntegerSchema(String errorMsg) {
        super(NumberType.INT);
        super.parseError(errorMsg);
    }
}