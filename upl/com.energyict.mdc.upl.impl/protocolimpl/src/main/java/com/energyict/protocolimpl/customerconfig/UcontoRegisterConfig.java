/*
 * UcontoRegisterConfig.java
 *
 * Created on 4 januari 2005, 17:44
 */

package com.energyict.protocolimpl.customerconfig;

import com.energyict.obis.ObisCode;

import java.util.Map;

/**
 *
 * @author  Koen
 */
public class UcontoRegisterConfig extends RegisterConfig {

    public final int SCALER=3;

    public UcontoRegisterConfig() {
        super();
    }

    protected Map<ObisCode, Register> getRegisterMap() {
        return map;
    }

    public int getScaler() {
        return SCALER;
    }

    protected void initRegisterMap() {
        map.put(ObisCode.fromString("1.1.1.8.0.255"),new Register(null,0));
        map.put(ObisCode.fromString("1.1.2.8.0.255"),new Register(null,1));
        map.put(ObisCode.fromString("1.1.3.8.0.255"),new Register(null,2));
        map.put(ObisCode.fromString("1.1.4.8.0.255"),new Register(null,3));
        map.put(ObisCode.fromString("1.2.1.8.0.255"),new Register(null,4));
    }

}