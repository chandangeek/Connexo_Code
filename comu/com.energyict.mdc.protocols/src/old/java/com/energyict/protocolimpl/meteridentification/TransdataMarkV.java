/*
 * TransdataMarkV.java
 *
 * Created on 10 augustus 2005, 9:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;

/**
 *
 * @author koen
 */
public class TransdataMarkV extends AbstractManufacturer {

    public String getManufacturer() throws IOException {
        return "Transdata";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.transdata.markv.MarkV";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "Transdata MarkV";
    }
}
