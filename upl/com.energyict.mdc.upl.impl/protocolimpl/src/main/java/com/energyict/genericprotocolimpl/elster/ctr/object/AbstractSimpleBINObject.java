package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractSimpleBINObject extends AbstractCTRObject {

    private CTRObjectValue[] value; //Binary value, with its unit & an overflowValue. 
    private BigDecimal def;

    public CTRObjectValue[] getValue() {
            return value;
        }
    protected void setValue(CTRObjectValue[] value) {
        this.value = value;
    }

    public BigDecimal getDefault() {
        return def;
    }
    protected void setDefault(BigDecimal def) {
        this.def = def;
    }
}
