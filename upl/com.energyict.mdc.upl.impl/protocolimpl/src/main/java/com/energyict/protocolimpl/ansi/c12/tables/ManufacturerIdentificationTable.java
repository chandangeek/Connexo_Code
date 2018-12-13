/*
 * ManufacturerIdentificationTable.java
 *
 * Created on 18 oktober 2005, 12:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.base.FirmwareVersion;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ManufacturerIdentificationTable extends AbstractTable {
    
    private String manufacturer;
    private String model;
    private int hwVersion;
    private int hwRevision;
    private int fwVersion;
    private int fwRevision;
    private String manufacturerSerialNumber;
    FirmwareVersion firmwareVersion;
    
    /** Creates a new instance of ManufacturerIdentificationTable */
    public ManufacturerIdentificationTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(1));
    }
    
    public String toString() {
       return "ManufacturerIdentificationTable: manufacturer="+getManufacturer()+", model="+getModel()+", hwVersion="+getHwVersion()+", fwVersion="+getFwVersion()+", hwRevision="+getHwRevision()+", fwRevision="+getFwRevision()+", manufacturerSerialNumber="+getManufacturerSerialNumber()+"\n";    
    }
    
    protected void parse(byte[] tableData) throws IOException {
        setManufacturer((new String(ProtocolUtils.getSubArray2(tableData,0,4))).trim());
        setModel((new String(ProtocolUtils.getSubArray2(tableData,4,8))).trim());
        setHwVersion(C12ParseUtils.getInt(tableData,12));
        setHwRevision(C12ParseUtils.getInt(tableData,13));
        setFwVersion(C12ParseUtils.getInt(tableData,14));
        setFwRevision(C12ParseUtils.getInt(tableData,15));
        
        if (getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getIdForm()==1)
            setManufacturerSerialNumber((new String(ProtocolUtils.getSubArray2(tableData,16,8))).trim());
        else
            setManufacturerSerialNumber((new String(ProtocolUtils.getSubArray2(tableData,16,16))).trim());
            
        firmwareVersion = new FirmwareVersion(Integer.toString(getFwVersion())+"."+Integer.toString(getFwRevision()));
    }

    public FirmwareVersion getFirmwareVersion() {
        return firmwareVersion;
    }
    
    
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(int hwVersion) {
        this.hwVersion = hwVersion;
    }

    public int getHwRevision() {
        return hwRevision;
    }

    public void setHwRevision(int hwRevision) {
        this.hwRevision = hwRevision;
    }

    public int getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(int fwVersion) {
        this.fwVersion = fwVersion;
    }

    public int getFwRevision() {
        return fwRevision;
    }

    public void setFwRevision(int fwRevision) {
        this.fwRevision = fwRevision;
    }

    public String getManufacturerSerialNumber() {
        return manufacturerSerialNumber;
    }

    public void setManufacturerSerialNumber(String manufacturerSerialNumber) {
        this.manufacturerSerialNumber = manufacturerSerialNumber;
    }


}
