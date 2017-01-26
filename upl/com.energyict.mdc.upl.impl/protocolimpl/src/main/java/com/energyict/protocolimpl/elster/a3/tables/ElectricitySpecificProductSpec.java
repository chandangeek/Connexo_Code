/*
 * ElectricitySpecificProductSpec.java
 *
 * Created on 10 februari 2006, 15:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.ManufacturerIdentificationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ElectricitySpecificProductSpec extends AbstractTable { 
/*
    Memory: storage EEPROM
    Total table size: (bytes) 43, Fixed
    Read access: 0
    Write access: Restricted
 
    Most data elements in MT-1 are written by Elster Electricity software. Some data elements
    are updated automatically by the A3 firmware.
    This table provides storage for Elster Electricity specific version and revision information
    required to identify the meter.
    The meter will set the Option Board 1 and Option Board 2 fields for the XMB and the outage
    modem. These are the only 'smart' boards; other option board information may be set by
    Elster Electricity software. When an XMB is present, the meter will set the Option Board 1
    fields using information read from the XMB. When an outage modem is present, the meter
    will set Option Board 2 fields using information read from the outage modem.
    Option Board 1 fields will be cleared by the meter if they indicate that an XMB is present and
    the meter detects that an XMB is not present (assumes that the XMB was removed.) The same
    holds true for Option Board 2 fields and the outage modem.
            Option Board Types are:
            0A External modem board
            0B 20mA current loop board
            0D Internal modem
            0E RS232
            0F RS485
            0G Outage modem w/ battery, SSPEC = 000225 00 32
            0H WAN board
            0J Cellnet
            0K 2-modem board
            11 No Comm board with relays
            12 XMB, SSPEC = 000224 00 02
            13 Modem interface adapter board
            14 Pulse input board
    At this time, Elster Electricity software is not populating option board fields for non-smart boards.
*/
    
    private String mfgSmartStyle; // 11 bytes Reserved for the smart style number as manufactured. Set by manufacturing; not used by the meter. 
    private OptionBoardDefinition[] optionBoardDefinitions; // 2 when fw < 2.1, 8 when fw >= 2.1 6 extra added at end of table
    private int firmwareSSPEC; // 3  bytes The Elster Electricity assigned A3 ALPHA firmware S-Spec number per the ROM release. Not writable, set in ROM. 
    private String dspIdentificationCode; // 2 bytes This 2-byte value is unique for each DSP code set. 
    private int dspVersion; // 1 bytes The Elster Electricity factory defined DSP version for the identified code set. 
    private int smVersion; // 2 bytes The Elster Electricity factory version of "Source Measurement" as defined in MT-17. Writes to this field are ignored. The version number is changed if the change impacts software support of the meter, otherwise only the revision number is changed. SM_Version will initially be set as follows: D/T Meters = 0 R = 1 K = 2 
    private int smRevision; // 1 bytes The Elster Electricity factory revision of "Source Measurement" as defined in MT-17. Writes to this field are ignored. Initially, SM_Revision will be set to zero in the first release. 
    private int pqmVersion; // 2 bytes The factory version of PQM. Writes to this field are ignored. 
    private int pqmRevision; // 1 byte The factory revision of PQM. Writes to this field are ignored. 
    private int serviceVersion; // 2 bytes The factory version of service voltage and service current tests. Writes to this field are ignored. 
    private int serviceRevision; // 1 byte The factory revision of service voltage and service current tests. Writes to this field are ignored. 
    private int currentKeyConfiguration; // 3 bytes Current KEY configuration. Writes to MT-1.CurrentKeyConfiguration have no effect. 
    /*
    Byte 0: This byte is informational only. The meter does not use byte 0 to determine functionality. 
            b0-1:      Meter DSP type. 
                  0 = Watt only (A3D or A3T, depending on b4 & b5 below) 
                  1 = Watt / VAR (A3R)       2 = Watt / VA (A3K)       3 = undefined b2-7:      Undefined. 
    Bytes 1 - 2: These bits define whether the specified functionality is enabled (1) or disabled (0). 
            b0:      Profiling 
            b1:      Instrumentation Profiling b2:      PQM b3:      Advanced Metering b4:      Timekeeping b5:      TOU b6:      Loss Compensation b7-b15:      not defined and not used by the meter. 
    */
    
    /** Creates a new instance of ElectricitySpecificProductSpec */
    public ElectricitySpecificProductSpec(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(1,true));
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ElectricitySpecificProductSpec:\n");
        strBuff.append("   currentKeyConfiguration=0x"+Integer.toHexString(getCurrentKeyConfiguration())+"\n");
        strBuff.append("   dspIdentificationCode="+getDspIdentificationCode()+"\n");
        strBuff.append("   dspVersion=0x"+Integer.toHexString(getDspVersion())+"\n");
        strBuff.append("   firmwareSSPEC=0x"+Integer.toHexString(getFirmwareSSPEC())+"\n");
        strBuff.append("   mfgSmartStyle="+getMfgSmartStyle()+"\n");
        strBuff.append("   pqmRevision="+getPqmRevision()+"\n");
        strBuff.append("   pqmVersion="+getPqmVersion()+"\n");
        strBuff.append("   serviceRevision="+getServiceRevision()+"\n");
        strBuff.append("   serviceVersion="+getServiceVersion()+"\n");
        strBuff.append("   smRevision="+getSmRevision()+"\n");
        strBuff.append("   smVersion="+getSmVersion()+"\n");
        for (int i=0;i<getOptionBoardDefinitions().length;i++)
            strBuff.append("   optionBoardDefinitions["+i+"]="+getOptionBoardDefinitions()[i]+"\n");
        return strBuff.toString();
    }
    
    
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        ManufacturerIdentificationTable manufacturerIdentificationTable = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getManufacturerIdentificationTable();
        boolean firmwareVersion21=false;
        if ((manufacturerIdentificationTable.getFwVersion()>=2) && (manufacturerIdentificationTable.getFwRevision()>=1)) {
            setOptionBoardDefinitions(new OptionBoardDefinition[8]);
            firmwareVersion21 = true;
        }
        else
            setOptionBoardDefinitions(new OptionBoardDefinition[2]);
        
        int offset=0;
        setMfgSmartStyle(new String(ProtocolUtils.getSubArray2(tableData,offset,11))); offset+=11;
        
        for (int i=0;i<2;i++) {
            getOptionBoardDefinitions()[i] = new OptionBoardDefinition(tableData, offset, getTableFactory()); 
            offset+=OptionBoardDefinition.getSize(getTableFactory());
        }
        
        setFirmwareSSPEC(ProtocolUtils.getInt(tableData,offset,3)); offset+=3; // SSPEC should not depend on dataOrder
        setDspIdentificationCode(new String(ProtocolUtils.getSubArray2(tableData, offset, 2))); offset+=2;
        setDspVersion(C12ParseUtils.getInt(tableData,offset)); offset++;
        setSmVersion(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setSmRevision(C12ParseUtils.getInt(tableData,offset)); offset++;
        setPqmVersion(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2; 
        setPqmRevision(C12ParseUtils.getInt(tableData,offset)); offset++;
        setServiceVersion(C12ParseUtils.getInt(tableData,offset,2,dataOrder)); offset+=2;
        setServiceRevision(C12ParseUtils.getInt(tableData,offset)); offset++;
        setCurrentKeyConfiguration(C12ParseUtils.getInt(tableData,offset,3,dataOrder)); offset+=3;
        
        if (firmwareVersion21) {
            for (int i=2;i<8;i++)
                getOptionBoardDefinitions()[i] = new OptionBoardDefinition(tableData, offset, getTableFactory()); offset+=OptionBoardDefinition.getSize(getTableFactory());
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

    public String getMfgSmartStyle() {
        return mfgSmartStyle;
    }

    public void setMfgSmartStyle(String mfgSmartStyle) {
        this.mfgSmartStyle = mfgSmartStyle;
    }

    public int getFirmwareSSPEC() {
        return firmwareSSPEC;
    }

    public void setFirmwareSSPEC(int firmwareSSPEC) {
        this.firmwareSSPEC = firmwareSSPEC;
    }


    public int getDspVersion() {
        return dspVersion;
    }

    public void setDspVersion(int dspVersion) {
        this.dspVersion = dspVersion;
    }

    public int getSmVersion() {
        return smVersion;
    }

    public void setSmVersion(int smVersion) {
        this.smVersion = smVersion;
    }

    public int getSmRevision() {
        return smRevision;
    }

    public void setSmRevision(int smRevision) {
        this.smRevision = smRevision;
    }

    public int getPqmVersion() {
        return pqmVersion;
    }

    public void setPqmVersion(int pqmVersion) {
        this.pqmVersion = pqmVersion;
    }

    public int getPqmRevision() {
        return pqmRevision;
    }

    public void setPqmRevision(int pqmRevision) {
        this.pqmRevision = pqmRevision;
    }

    public int getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(int serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public int getServiceRevision() {
        return serviceRevision;
    }

    public void setServiceRevision(int serviceRevision) {
        this.serviceRevision = serviceRevision;
    }

    public int getCurrentKeyConfiguration() {
        return currentKeyConfiguration;
    }

    public void setCurrentKeyConfiguration(int currentKeyConfiguration) {
        this.currentKeyConfiguration = currentKeyConfiguration;
    }

    public OptionBoardDefinition[] getOptionBoardDefinitions() {
        return optionBoardDefinitions;
    }

    public void setOptionBoardDefinitions(OptionBoardDefinition[] optionBoardDefinitions) {
        this.optionBoardDefinitions = optionBoardDefinitions;
    }

    public String getDspIdentificationCode() {
        return dspIdentificationCode;
    }

    public void setDspIdentificationCode(String dspIdentificationCode) {
        this.dspIdentificationCode = dspIdentificationCode;
    }
        


}
