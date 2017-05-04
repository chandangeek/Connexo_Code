/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class RegisterType32BitSignedInt extends AbstractRegisterType {

    private int value;

    /** Creates a new instance of RegisterType16BitsInt */
    public RegisterType32BitSignedInt(byte[] data) throws IOException {

       setValue(ProtocolUtils.getInt(data,0,4));

    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
