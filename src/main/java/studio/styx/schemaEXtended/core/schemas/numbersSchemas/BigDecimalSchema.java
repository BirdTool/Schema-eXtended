package studio.styx.schemaEXtended.core.schemas.numbersSchemas;

import studio.styx.schemaEXtended.core.interfaces.NumberType;
import java.math.BigDecimal;

public class BigDecimalSchema extends NumberSchema<BigDecimal> {
    public BigDecimalSchema() {
        super(NumberType.BIGDECIMAL);
    }
    public BigDecimalSchema(String errorMsg) {
        super(NumberType.BIGDECIMAL);
        super.parseError(errorMsg);
    }
}