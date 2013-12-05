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

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class KV extends AbstractManufacturer {

    /** Creates a new instance of KV */
    public KV() {
    }

    public String getManufacturer() throws IOException {
        return "General Electric Industrial";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.ge.kv.GEKV";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "General Electric Industrial KV";
    }
}
