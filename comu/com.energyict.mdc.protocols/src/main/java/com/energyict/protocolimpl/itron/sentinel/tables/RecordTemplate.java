/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;


import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocolimpl.ansi.c12.tables.*;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocol.*;

/**
 *
 * @author Koen
 */
public class RecordTemplate {
    
    /** Creates a new instance of RecordTemplate */
    public RecordTemplate(byte[] tableData,int offset,ManufacturerTableFactory manufacturerTableFactory) throws IOException {
        ActualRegisterTable art = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        ActualLogTable alt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        
        
        
    }
    
//    public RecordTemplate() {
//    }
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new RecordTemplate()));
//    }     
    
    static public int getSize(ManufacturerTableFactory manufacturerTableFactory) throws IOException {
        ActualRegisterTable art = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        
        return 0;
    }      
}
