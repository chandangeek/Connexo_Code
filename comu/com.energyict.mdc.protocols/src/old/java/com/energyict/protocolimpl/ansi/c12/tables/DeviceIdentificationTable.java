/*
 * DeviceIdentificationTable.java
 *
 * Created on 26 oktober 2005, 13:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class DeviceIdentificationTable extends AbstractTable {

    private String identification;

    /** Creates a new instance of DeviceIdentificationTable */
    public DeviceIdentificationTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(5));
    }

    public String toString() {
        return "DeviceIdentificationTable: identification="+identification+"\n";
    }

    protected void parse(byte[] tableData) throws IOException {
        if (getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getIdForm() == 1) {
            // BCD
            setIdentification(new String(ProtocolUtils.convertAscii2Binary(tableData)));
        }
        else {
            // String
            setIdentification(new String(tableData));
        }

    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

}
