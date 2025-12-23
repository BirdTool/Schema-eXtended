package studio.styx.schemaEXtended.core.schemas.numbersSchemas;

import studio.styx.schemaEXtended.core.interfaces.NumberType;

public class DoubleSchema extends NumberSchema<Double> {
    public DoubleSchema() {
        super(NumberType.DOUBLE);
    }
    public DoubleSchema(String errorMsg) {
        super(NumberType.DOUBLE);
        super.parseError(errorMsg);
    }
}