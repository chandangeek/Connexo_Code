/*
 * TableTemplate.java
 *
 * Created July 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class S4Configuration extends AbstractTable {

    private S4ConfigMask s4ConfigMask;

    /** Creates a new instance of TableTemplate */
    public S4Configuration(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(13,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("S4Configuration:\n");
        strBuff.append("    s4ConfigMask="+getS4ConfigMask()+"\n");;
        return strBuff.toString();
    }


    protected void parse(byte[] tableData) throws IOException {
        setS4ConfigMask(new S4ConfigMask(tableData, 0, getManufacturerTableFactory()));
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public S4ConfigMask getS4ConfigMask() {
        return s4ConfigMask;
    }

    public void setS4ConfigMask(S4ConfigMask s4ConfigMask) {
        this.s4ConfigMask = s4ConfigMask;
    }


}
