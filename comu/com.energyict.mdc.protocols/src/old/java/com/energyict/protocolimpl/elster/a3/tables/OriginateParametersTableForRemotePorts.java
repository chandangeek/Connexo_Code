/*
 * OriginateParametersTableForRemotePorts.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class OriginateParametersTableForRemotePorts extends AbstractTable {

    /*
    Memory storage: EEPROM
    Total table size: (bytes) 203, Fixed
    Read access: 1
    Write access: 3
    */

    private int dialDelay; // 1 byte This field will be forced to zero by the meter. Commas in the dial string can be used to force a delay between going off-hook and dialing of the phone number. For example, ATDT,1,9192505418 would insert a delay between going offhook and dialing the '1' and between the '1' and the 919.
    private String[] phoneNumbers; // 3 phone numbers of 64 bytes long
    private Window[] windows; // 2 windows

    /** Creates a new instance of OriginateParametersTableForRemotePorts */
    public OriginateParametersTableForRemotePorts(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(93,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OriginateParametersTableForRemotePorts:\n");
        strBuff.append("   dialDelay="+getDialDelay()+"\n");
        for (int i=0;i<getPhoneNumbers().length;i++)
            strBuff.append("   phoneNumbers["+i+"]="+getPhoneNumbers()[i]+"\n");
        for (int i=0;i<getWindows().length;i++)
            strBuff.append("   windows["+i+"]="+getWindows()[i]+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setDialDelay(C12ParseUtils.getInt(tableData,offset++));
        setPhoneNumbers(new String[3]);
        for (int i=0;i<getPhoneNumbers().length;i++) {
            getPhoneNumbers()[i] = new String(ProtocolUtils.getSubArray2(tableData, offset, 64)); offset+=64;
        }
        setWindows(new Window[2]);
        for (int i=0;i<getWindows().length;i++) {
            getWindows()[i] = new Window(tableData, offset, getTableFactory());
            offset+=Window.getSize(getTableFactory());
        }
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public int getDialDelay() {
        return dialDelay;
    }

    public void setDialDelay(int dialDelay) {
        this.dialDelay = dialDelay;
    }

    public String[] getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String[] phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public Window[] getWindows() {
        return windows;
    }

    public void setWindows(Window[] windows) {
        this.windows = windows;
    }

}
