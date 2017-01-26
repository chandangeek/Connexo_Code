/*
 * RegisterType16BitsInt.java
 *
 * Created on 22 maart 2006, 9:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterType64BitSignedLong extends AbstractRegisterType {

    private long value;

    /** Creates a new instance of RegisterType16BitsInt */
    public RegisterType64BitSignedLong(byte[] data) throws ProtocolException {
       setValue(ProtocolUtils.getLong(data,0,8));
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+value);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
