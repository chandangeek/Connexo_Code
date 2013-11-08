/*
 * TableTemplate.java
 *
 * Created on 28 oktober 2005, 17:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocol.*;

/**
 *
 * @author Koen
 */
public class TableTemplate extends AbstractTable {
    
    /** Creates a new instance of TableTemplate */
    public TableTemplate(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(-1));
    }
    
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TableTemplate(null)));
//    }     
    
    protected void parse(byte[] tableData) throws IOException { 
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        
        int offset=0;
    }         
}
