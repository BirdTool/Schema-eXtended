package studio.styx.schemaEXtended.core.schemas.numbersSchemas;

import studio.styx.schemaEXtended.core.interfaces.NumberType;

public class FloatSchema extends NumberSchema<Float> {
    public FloatSchema() {
        super(NumberType.FLOAT);
    }
    public FloatSchema(String errorMsg) {
        super(NumberType.FLOAT);
        super.parseError(errorMsg);
    }
}