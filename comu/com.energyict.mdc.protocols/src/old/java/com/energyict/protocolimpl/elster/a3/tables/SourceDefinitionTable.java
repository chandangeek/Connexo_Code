/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SourceDefinitionTable.java
 *
 * Created on 11 februari 2006, 13:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SourceDefinitionTable extends AbstractTable { 
    /*
    Memory storage:	EEPROM
    Total table size: (bytes)	1785
    Read access:	1
    Write access:	Restricted

    MT-17 is the list of sources or metered quantities. The following components select metered quantities from MT-17:	
            * billing data (ST-22 selects MT-17	pulse sources for ST-23)
       * present values (ST-27 selects MT-17 instrumentation source for ST-28)   
       * display lists (metered quantities and instrumentation sources are referenced by their MT-17 source id)   
       * load profiling (ST-62 selects pulse sources for ST-64)   
       * instrumentation profiling (MT-62 selects pulse and/or instrumentation sources for MT-64 and MT-65)  
    Each MT-17 entry points to an entry in ST-13 and ST-15 to further define the source. 
    Although the table provides the mechanism to select an entry in ST-13, 
    The A3 will always use the first "demand interval" entry in ST-13 during normal mode and the second "demand interval" entry in ST-13 during test mode. 
    All source entries in MT-17 will be set to point to the first "demand interval" entry in ST-13. 
    MT-17 does not dictate the time base, thereby not limiting a source to a summation or a demand. 
    This allows the same pulse source listed in MT-17 to be used for summations, demands, and load profile data. 
    Note that the divisor to be applied to pulse counts prior to storing load profile interval data is specified in ST-62.  
    The first 50 sources are pulse sources. The first 8 sources listed in MT-17 must be defined as the 'raw' pulse sources consistent with the meter type.
    The next 4 sources are reserved for relay pulse inputs. The remaining 38 pulse sources are calculated. 
    To support the different pulse sources required based on meter type, there are 3 different versions of MT-17.  
            * watt only meter (used for A3D and A3T meters)  
            * VAR meter with arithmetically calculated VA (used for A3R meters)  
            * VA meter with arithmetically calculated VAR (used for A3K meters) 
    The remaining 205 entries are reserved for instrumentation sources. The list of instrumentation sources is the same for all meter types. 
    There are 37 entries for basic instrumentation sources and 78 entries reserved for per phase voltage and current harmonic sources. 
    The last 90 entries in MT-17 are reserved for calculated instrumentation sources. 

    */
    
    private SourceDefinitionEntry[] sourceDefinitionEntries;
    
    /** Creates a new instance of ElectricitySpecificProductSpec */
    public SourceDefinitionTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(17,true));
    }
 
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SourceDefinitionTable:\n");
        for (int i=0;i<getSourceDefinitionEntries().length;i++) {
            strBuff.append("    sourceDefinitionEntries["+i+"]="+getSourceDefinitionEntries()[i]+"\n");
        }    
        return strBuff.toString();
    }
    
    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;
        setSourceDefinitionEntries(new SourceDefinitionEntry[255]);
        for (int i=0;i<getSourceDefinitionEntries().length;i++) {
            getSourceDefinitionEntries()[i] = new SourceDefinitionEntry(tableData,offset,getTableFactory());
            offset+=SourceDefinitionEntry.getSize(getTableFactory());
        }
    } 
    
    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }
    
//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public SourceDefinitionEntry[] getSourceDefinitionEntries() {
        return sourceDefinitionEntries;
    }

    public void setSourceDefinitionEntries(SourceDefinitionEntry[] sourceDefinitionEntries) {
        this.sourceDefinitionEntries = sourceDefinitionEntries;
    }


}
