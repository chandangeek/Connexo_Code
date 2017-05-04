/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ConstantsDataRead extends AbstractDataRead {

    private float ctMultiplier;
    private float vtMultiplier;
    private float registerMultiplier;
    private String customerSerialNumber; // 10 chars
    private int programID; // UINT16
    private float firmwareVersionRevision;
    private int demandIntervalLength; // UINT8 (in minutes)
    private float phaseAVoltage;
    private float phaseACurrent;
    private float phaseACurrentAngle;
    private float phaseBVoltage;
    private float phaseBVoltageAngle;
    private float phaseBCurrent;
    private float phaseBCurrentAngle;
    private float phaseCVoltage;
    private float phaseCVoltageAngle;
    private float phaseCCurrent;
    private float PhaseCCurrentAngle;
    private int phaseADCDetect; // UINT16
    private int phaseBDCDetect; // UINT16
    private int phaseCDCDetect; // UINT16
    private int serviceTypeDetected; // UINT8
    private int abcPhaseRotation; // UINT8


    /** Creates a new instance of ConstantsDataRead */
    public ConstantsDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ConstantsDataRead:\n");
        strBuff.append("   abcPhaseRotation="+getAbcPhaseRotation()+"\n");
        strBuff.append("   ctMultiplier="+getCtMultiplier()+"\n");
        strBuff.append("   customerSerialNumber="+getCustomerSerialNumber()+"\n");
        strBuff.append("   demandIntervalLength="+getDemandIntervalLength()+"\n");
        strBuff.append("   firmwareVersionRevision="+getFirmwareVersionRevision()+"\n");
        strBuff.append("   phaseACurrent="+getPhaseACurrent()+"\n");
        strBuff.append("   phaseACurrentAngle="+getPhaseACurrentAngle()+"\n");
        strBuff.append("   phaseADCDetect="+getPhaseADCDetect()+"\n");
        strBuff.append("   phaseAVoltage="+getPhaseAVoltage()+"\n");
        strBuff.append("   phaseBCurrent="+getPhaseBCurrent()+"\n");
        strBuff.append("   phaseBCurrentAngle="+getPhaseBCurrentAngle()+"\n");
        strBuff.append("   phaseBDCDetect="+getPhaseBDCDetect()+"\n");
        strBuff.append("   phaseBVoltage="+getPhaseBVoltage()+"\n");
        strBuff.append("   phaseBVoltageAngle="+getPhaseBVoltageAngle()+"\n");
        strBuff.append("   phaseCCurrent="+getPhaseCCurrent()+"\n");
        strBuff.append("   phaseCCurrentAngle="+getPhaseCCurrentAngle()+"\n");
        strBuff.append("   phaseCDCDetect="+getPhaseCDCDetect()+"\n");
        strBuff.append("   phaseCVoltage="+getPhaseCVoltage()+"\n");
        strBuff.append("   phaseCVoltageAngle="+getPhaseCVoltageAngle()+"\n");
        strBuff.append("   programID="+getProgramID()+"\n");
        strBuff.append("   registerMultiplier="+getRegisterMultiplier()+"\n");
        strBuff.append("   serviceTypeDetected="+getServiceTypeDetected()+"\n");
        strBuff.append("   vtMultiplier="+getVtMultiplier()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setCtMultiplier(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setVtMultiplier(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setRegisterMultiplier(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;

        int count;
        for(count=0;count<10;count++) {
            if (data[offset+count] == 0) {
               count++;
               break;
            }
        }
        customerSerialNumber = new String(ProtocolUtils.getSubArray2(data,offset, count));
        offset+=10;

        setProgramID(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setFirmwareVersionRevision(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setDemandIntervalLength(C12ParseUtils.getInt(data,offset++));
        setPhaseAVoltage(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseACurrent(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseACurrentAngle(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseBVoltage(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseBVoltageAngle(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseBCurrent(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseBCurrentAngle(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseCVoltage(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseCVoltageAngle(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseCCurrent(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseCCurrentAngle(Float.intBitsToFloat(C12ParseUtils.getInt(data,offset,4, dataOrder)));
        offset+=4;
        setPhaseADCDetect(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setPhaseBDCDetect(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setPhaseCDCDetect(C12ParseUtils.getInt(data,offset,2, dataOrder));
        offset+=2;
        setServiceTypeDetected(C12ParseUtils.getInt(data,offset++));
    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("CT_MULTIPLIER").getId(),
                                 LogicalIDFactory.findLogicalId("VT_MULTIPLIER").getId(),
                                 LogicalIDFactory.findLogicalId("REGISTER_MULTIPLIER").getId(),
                                 LogicalIDFactory.findLogicalId("CUSTOMER_SERIAL_NUMBER").getId(),
                                 LogicalIDFactory.findLogicalId("PROGRAM_ID").getId(),
                                 LogicalIDFactory.findLogicalId("FIRMWARE_VERSION_REVISION").getId(),
                                 LogicalIDFactory.findLogicalId("DEMAND_INTERVAL_LENGTH").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SITESCAN").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, 0x08, lids));

    } // protected void prepareBuild() throws IOException

    public float getCtMultiplier() {
        return ctMultiplier;
    }

    public void setCtMultiplier(float ctMultiplier) {
        this.ctMultiplier = ctMultiplier;
    }

    public float getVtMultiplier() {
        return vtMultiplier;
    }

    public void setVtMultiplier(float vtMultiplier) {
        this.vtMultiplier = vtMultiplier;
    }

    public float getRegisterMultiplier() {
        return registerMultiplier;
    }

    public void setRegisterMultiplier(float registerMultiplier) {
        this.registerMultiplier = registerMultiplier;
    }

    public String getCustomerSerialNumber() {
        return customerSerialNumber;
    }

    public void setCustomerSerialNumber(String customerSerialNumber) {
        this.customerSerialNumber = customerSerialNumber;
    }

    public int getProgramID() {
        return programID;
    }

    public void setProgramID(int programID) {
        this.programID = programID;
    }

    public float getFirmwareVersionRevision() {
        return firmwareVersionRevision;
    }

    public void setFirmwareVersionRevision(float firmwareVersionRevision) {
        this.firmwareVersionRevision = firmwareVersionRevision;
    }

    public int getDemandIntervalLength() {
        return demandIntervalLength;
    }

    public void setDemandIntervalLength(int demandIntervalLength) {
        this.demandIntervalLength = demandIntervalLength;
    }

    public float getPhaseAVoltage() {
        return phaseAVoltage;
    }

    public void setPhaseAVoltage(float phaseAVoltage) {
        this.phaseAVoltage = phaseAVoltage;
    }

    public float getPhaseACurrent() {
        return phaseACurrent;
    }

    public void setPhaseACurrent(float phaseACurrent) {
        this.phaseACurrent = phaseACurrent;
    }

    public float getPhaseACurrentAngle() {
        return phaseACurrentAngle;
    }

    public void setPhaseACurrentAngle(float phaseACurrentAngle) {
        this.phaseACurrentAngle = phaseACurrentAngle;
    }

    public float getPhaseBVoltage() {
        return phaseBVoltage;
    }

    public void setPhaseBVoltage(float phaseBVoltage) {
        this.phaseBVoltage = phaseBVoltage;
    }

    public float getPhaseBVoltageAngle() {
        return phaseBVoltageAngle;
    }

    public void setPhaseBVoltageAngle(float phaseBVoltageAngle) {
        this.phaseBVoltageAngle = phaseBVoltageAngle;
    }

    public float getPhaseBCurrent() {
        return phaseBCurrent;
    }

    public void setPhaseBCurrent(float phaseBCurrent) {
        this.phaseBCurrent = phaseBCurrent;
    }

    public float getPhaseBCurrentAngle() {
        return phaseBCurrentAngle;
    }

    public void setPhaseBCurrentAngle(float phaseBCurrentAngle) {
        this.phaseBCurrentAngle = phaseBCurrentAngle;
    }

    public float getPhaseCVoltage() {
        return phaseCVoltage;
    }

    public void setPhaseCVoltage(float phaseCVoltage) {
        this.phaseCVoltage = phaseCVoltage;
    }

    public float getPhaseCVoltageAngle() {
        return phaseCVoltageAngle;
    }

    public void setPhaseCVoltageAngle(float phaseCVoltageAngle) {
        this.phaseCVoltageAngle = phaseCVoltageAngle;
    }

    public float getPhaseCCurrent() {
        return phaseCCurrent;
    }

    public void setPhaseCCurrent(float phaseCCurrent) {
        this.phaseCCurrent = phaseCCurrent;
    }

    public float getPhaseCCurrentAngle() {
        return PhaseCCurrentAngle;
    }

    public void setPhaseCCurrentAngle(float PhaseCCurrentAngle) {
        this.PhaseCCurrentAngle = PhaseCCurrentAngle;
    }

    public int getPhaseADCDetect() {
        return phaseADCDetect;
    }

    public void setPhaseADCDetect(int phaseADCDetect) {
        this.phaseADCDetect = phaseADCDetect;
    }

    public int getPhaseBDCDetect() {
        return phaseBDCDetect;
    }

    public void setPhaseBDCDetect(int phaseBDCDetect) {
        this.phaseBDCDetect = phaseBDCDetect;
    }

    public int getPhaseCDCDetect() {
        return phaseCDCDetect;
    }

    public void setPhaseCDCDetect(int phaseCDCDetect) {
        this.phaseCDCDetect = phaseCDCDetect;
    }

    public int getServiceTypeDetected() {
        return serviceTypeDetected;
    }

    public void setServiceTypeDetected(int serviceTypeDetected) {
        this.serviceTypeDetected = serviceTypeDetected;
    }

    public int getAbcPhaseRotation() {
        return abcPhaseRotation;
    }

    public void setAbcPhaseRotation(int abcPhaseRotation) {
        this.abcPhaseRotation = abcPhaseRotation;
    }

} // public class ConstantsDataRead extends AbstractDataRead
