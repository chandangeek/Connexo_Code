/*
 * ReadOnlyTable2050.java
 *
 * Created on 02112006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ReadOnlyTable2050 extends AbstractTable {

    private byte[] data;


    /** Creates a new instance of ReadOnlyTable2050 */
    public ReadOnlyTable2050(ManufacturerTableFactory manufacturerTableFactory) {
        //super(manufacturerTableFactory,new TableIdentification(1,true)); // alternative way of declaration
        super(manufacturerTableFactory,new TableIdentification(2050));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadOnlyTable2050:\n");
        for (int i=0;i<getData().length;i++) {
            strBuff.append("       data["+i+"]="+getData()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;
        setData(tableData);

    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


}
