/*
 * KV.java
 *
 * Created on 26 oktober 2005, 16:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.meteridentification;

/**
 *
 * @author Koen
 */
public class KV extends AbstractManufacturer {

    public String getManufacturer() {
        return "General Electric Industrial";
    }

    public String getMeterProtocolClass() {
        return "com.energyict.protocolimpl.ge.kv.GEKV";
    }

    public String[] getMeterSerialNumberRegisters() {
        return null;
    }

    public String getMeterDescription() {
        return "General Electric Industrial KV";
    }

}