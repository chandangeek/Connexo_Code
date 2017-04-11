package com.energyict.protocolimpl.edmi.common.core;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author koen
 */
abstract public class AbstractRegisterType {



    /** Creates a new instance of RegisterType */
    public AbstractRegisterType() {
    }

    public String toString() {
        return "AbstractRegisterType: bigDecimal="+getBigDecimal()+", Date="+getDate()+", String="+getString();
    }

    public BigDecimal getBigDecimal() {
        return null;
    }
    public Date getDate() {
        return null;
    }
    public String getString() {
        return ""+getBigDecimal();
    }
}
