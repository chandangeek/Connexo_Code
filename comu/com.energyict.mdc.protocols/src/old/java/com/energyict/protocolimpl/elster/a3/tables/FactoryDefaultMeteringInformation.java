/*
 * FactoryDefaultMeteringInformation.java
 *
 * Created on 11 februari 2006, 11:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class FactoryDefaultMeteringInformation extends AbstractTable {

    /*
    Memory: storage EEPROM
    Total table size: (bytes) 15, Fixed
    Read access: 1
    Write access: Restricted
    */

    private int instrumentationScale; // 1 byte INT8. The scale factor for all instrumentation sources (50-254) in MT-17. This Factor (ISF)      will be set at the factory based on meter class: <= 20 A, instrumentation scale factor = -4       > 20 A, instrumentation scale factor = -3
    //RESERVED 14 bytes


    /** Creates a new instance of ElectricitySpecificProductSpec */
    public FactoryDefaultMeteringInformation(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(16,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FactoryDefaultMeteringInformation:\n");
        strBuff.append("   instrumentationScale="+getInstrumentationScale()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setInstrumentationScale((int)C12ParseUtils.getExtendedLong(tableData,offset++,1, dataOrder));
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }
//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public int getInstrumentationScale() {
        return instrumentationScale;
    }

    public void setInstrumentationScale(int instrumentationScale) {
        this.instrumentationScale = instrumentationScale;
    }

}
