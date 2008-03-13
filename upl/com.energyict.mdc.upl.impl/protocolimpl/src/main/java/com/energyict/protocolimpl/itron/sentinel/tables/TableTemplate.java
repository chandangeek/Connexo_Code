/*
 * WriteOnlyTable2049.java
 *
 * Created on 02112006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import java.io.*;
import java.util.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;

/**
 *
 * @author Koen
 */
public class TableTemplate extends AbstractTable { 

    /** Creates a new instance of WriteOnlyTable2049 */
    public TableTemplate(ManufacturerTableFactory manufacturerTableFactory) {
        //super(manufacturerTableFactory,new TableIdentification(1,true)); // alternative way of declaration
        super(manufacturerTableFactory,new TableIdentification(-1));
    }
 
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TableTemplate(null)));
//    }    
    
    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;

    } 
    
    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }
    
//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }


}
