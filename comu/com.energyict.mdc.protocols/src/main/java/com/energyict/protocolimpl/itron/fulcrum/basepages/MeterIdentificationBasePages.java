/*
 * MeterIdentificationBasePages.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MeterIdentificationBasePages extends AbstractBasePage {

    private String meterId;
    private String softwareRevisionLevel;
    private String firmwareRevisionLevel;
    private int programId;
    private String unitType;
    private String unitId;


    /** Creates a new instance of MeterIdentificationBasePages */
    public MeterIdentificationBasePages(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterIdentificationBasePages:\n");
        strBuff.append("   firmwareRevisionLevel="+getFirmwareRevisionLevel().trim()+"\n");
        strBuff.append("   meterId="+getMeterId().trim()+"\n");
        strBuff.append("   programId="+getProgramId()+"\n");
        strBuff.append("   softwareRevisionLevel="+getSoftwareRevisionLevel().trim()+"\n");
        strBuff.append("   unitId="+getUnitId().trim()+"\n");
        strBuff.append("   unitType="+getUnitType().trim()+"\n");
        return strBuff.toString();
    }

    public String toString2() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("firmwareRevisionLevel="+getFirmwareRevisionLevel()+", ");
        //strBuff.append("meterId="+getMeterId()+", ");
        strBuff.append("programId="+getProgramId()+", ");
        strBuff.append("softwareRevisionLevel="+getSoftwareRevisionLevel()+", ");
        strBuff.append("unitId="+getUnitId()+", ");
        strBuff.append("unitType="+getUnitType());
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x213C,0x215E-0x213C);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        if (ParseUtils.getBCD2Long(data, offset, 9) == 0)
             setMeterId("UNDEFINED");
        else
             setMeterId(new String(ProtocolUtils.getSubArray2(data, offset, 9)));
        offset+=9;
        setSoftwareRevisionLevel(new String(ProtocolUtils.getSubArray2(data, offset, 6)));
        offset+=6;
        setFirmwareRevisionLevel(new String(ProtocolUtils.getSubArray2(data, offset, 6)));
        offset+=6;
        setProgramId(ProtocolUtils.getInt(data, offset, 2));
        offset+=2;
        setUnitType(new String(ProtocolUtils.getSubArray2(data, offset, 3)));
        offset+=3;
        setUnitId(new String(ProtocolUtils.getSubArray2(data, offset, 8)));
        offset+=8;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getSoftwareRevisionLevel() {
        return softwareRevisionLevel;
    }

    public void setSoftwareRevisionLevel(String softwareRevisionLevel) {
        this.softwareRevisionLevel = softwareRevisionLevel;
    }

    public String getFirmwareRevisionLevel() {
        return firmwareRevisionLevel;
    }

    public void setFirmwareRevisionLevel(String firmwareRevisionLevel) {
        this.firmwareRevisionLevel = firmwareRevisionLevel;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }


} // public class RealTimeBasePage extends AbstractBasePage
