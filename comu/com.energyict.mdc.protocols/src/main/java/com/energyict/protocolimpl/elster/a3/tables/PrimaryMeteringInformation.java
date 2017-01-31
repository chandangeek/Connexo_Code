/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PrimaryMeteringInformation.java
 *
 * Created on 11 februari 2006, 10:56
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
public class PrimaryMeteringInformation extends AbstractTable { 
    
    /*
    Memory storage: EEPROM
    Total table size: (bytes) 20, Fixed
    Read access: 1
    Write access: 3     
    
    Contains the external multiplier for the meter installation. 
    These fields can be displayed but are otherwise not used by the meter. 
    MT-15 also contains the scale factor for all pulse sources that reference the adjusted Ke multiplier. 
    This table is not used by the meter for calculations. 
    MT-15 is only used to store information that can be read and displayed for the installation site. 
    */

    private int externalMultiplierScaleFactor; // 1 byte INT8. The scale factor applied to External Multiplier. This is set by Elster FACTOR      Electricity programming software. It is a power of 10, so the scale factor applied is 10^EXT_MULT_SCALE_FACTOR. Default = 0 
    private long externalMultiplier; // 4 bytes INT32. Binary with an implied scale factor of 10^EXT_MULT_SCALE_FACTOR. Applied externally to metered quantities to convert to engineering units. The external multiplier is NOT USED by the firmware. This is set by Elster Electricity programming software. The Adjusted Ke in ST-15 must already have been scaled to reflect the external multiplier (also done by Elster Electricity programming software).  
                             //Actual billing data value = billing data * external multiplier with a scale factor represented by Adjusted Ke Scale Factor.  
    private int adjustedKeScaleFactor; // 1 byte INT8. Implied power of 10 scale factor for all sources that use the ST-15 Adjusted Ke multiplier. This is set by Elster Electricity programming software. When MT-15 is written, this scale factor will be copied to the scale factor field for each MT-17 entry that points to the Adjusted Ke multiplier in ST-15. For example, a value of 3 indicates that all pulse source billing numbers will have units of milli (e.g. (mWH.) Example 4 =0xFC.
    private int adjustedKhScaleFactor; // 1 byte INT8. The scale factor applied to MT-15.Adjusted Kh. This is set by Elster Electricity programming software. It is a power of 10, so the scale factor applied is 10^ Adjusted Kh Scale Factor. 
    private long adjustedKh; // 6 bytes INT48. The actual Kh. This is set by Elster Electricity programming software. Adjusted Kh Scale Factor must be applied to this field to convert it to engineering units. This field can be displayed but is otherwise not used by the meter. 
    private int adjustedpr; // 1 byte INT8. The actual p/r. This is set by Elster Electricity programming software. This field is an integer and MT15_SCALE_FACTOR is not applied. This field can be displayed but is otherwise not used by the meter. Configuration software uses this value in other configuration fields in the meter. Typically: Reverse Power Test Threshold = 2 * Adjusted p/r Divisor for pulse block annunciator =  Adjusted p/r divided by 2. Test/Alt/Normal Optical Output Divisor =  Adjusted p/r divided by 2. 
    private long calculatedTransformer; // 6 bytes INT48. The transformer factor calculated by the meter. Calculated using ST-Factor      15.Instrumentation Mulitipliers as follows: VT Ratio * CT Ratio / 10ISF. 
    
    
    /** Creates a new instance of ElectricitySpecificProductSpec */
    public PrimaryMeteringInformation(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(15,true));
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PrimaryMeteringInformation:\n");
        strBuff.append("   adjustedKeScaleFactor="+getAdjustedKeScaleFactor()+"\n");
        strBuff.append("   adjustedKh="+getAdjustedKh()+"\n");
        strBuff.append("   adjustedKhScaleFactor="+getAdjustedKhScaleFactor()+"\n");
        strBuff.append("   adjustedpr="+getAdjustedpr()+"\n");
        strBuff.append("   calculatedTransformer="+getCalculatedTransformer()+"\n");
        strBuff.append("   externalMultiplier="+getExternalMultiplier()+"\n");
        strBuff.append("   externalMultiplierScaleFactor="+getExternalMultiplierScaleFactor()+"\n");
        return strBuff.toString();
    }
    
    
    
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        setExternalMultiplierScaleFactor((int)C12ParseUtils.getExtendedLong(tableData,offset++,1, dataOrder));
        setExternalMultiplier(C12ParseUtils.getExtendedLong(tableData,offset,4, dataOrder)); offset+=4;
        setAdjustedKeScaleFactor((int)C12ParseUtils.getExtendedLong(tableData,offset++,1, dataOrder));
        setAdjustedKhScaleFactor((int)C12ParseUtils.getExtendedLong(tableData,offset++,1, dataOrder));
        setAdjustedKh(C12ParseUtils.getExtendedLong(tableData,offset,6, dataOrder)); offset+=6;
        setAdjustedpr((int)C12ParseUtils.getExtendedLong(tableData,offset++,1, dataOrder));
        setCalculatedTransformer(C12ParseUtils.getExtendedLong(tableData,offset,6, dataOrder)); offset+=6;
    } 
    
    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }
    
//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }



    public int getExternalMultiplierScaleFactor() {
        return externalMultiplierScaleFactor;
    }

    public void setExternalMultiplierScaleFactor(int externalMultiplierScaleFactor) {
        this.externalMultiplierScaleFactor = externalMultiplierScaleFactor;
    }

    public long getExternalMultiplier() {
        return externalMultiplier;
    }

    public void setExternalMultiplier(long externalMultiplier) {
        this.externalMultiplier = externalMultiplier;
    }

    public int getAdjustedKeScaleFactor() {
        return adjustedKeScaleFactor;
    }

    public void setAdjustedKeScaleFactor(int adjustedKeScaleFactor) {
        this.adjustedKeScaleFactor = adjustedKeScaleFactor;
    }

    public int getAdjustedKhScaleFactor() {
        return adjustedKhScaleFactor;
    }

    public void setAdjustedKhScaleFactor(int adjustedKhScaleFactor) {
        this.adjustedKhScaleFactor = adjustedKhScaleFactor;
    }

    public long getAdjustedKh() {
        return adjustedKh;
    }

    public void setAdjustedKh(long adjustedKh) {
        this.adjustedKh = adjustedKh;
    }

    public int getAdjustedpr() {
        return adjustedpr;
    }

    public void setAdjustedpr(int adjustedpr) {
        this.adjustedpr = adjustedpr;
    }

    public long getCalculatedTransformer() {
        return calculatedTransformer;
    }

    public void setCalculatedTransformer(long calculatedTransformer) {
        this.calculatedTransformer = calculatedTransformer;
    }
}
