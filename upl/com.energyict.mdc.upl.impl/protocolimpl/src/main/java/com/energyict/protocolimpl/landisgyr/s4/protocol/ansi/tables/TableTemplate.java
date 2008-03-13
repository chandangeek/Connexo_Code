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

import java.io.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;
import java.math.*;

/**
 *
 * @author Koen
 */
public class TableTemplate extends AbstractTable {

      
      
    /** Creates a new instance of TableTemplate */
    public TableTemplate(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(0,true));
    }
    
    public TableTemplate() {
        super(null,null);
    }
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TableTemplate()));
    }    

    
    protected void parse(byte[] data) throws IOException {
        ConfigurationTable cfgt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualSourcesLimitingTable aslt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        int offset=0;
        //getT

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
