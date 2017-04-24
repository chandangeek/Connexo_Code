/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterIdentity.java
 *
 * Created on 7 juli 2004, 10:44
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class MeterIdentity extends AbstractLogicalAddress {

    // ****************************************************
    String meterId; // 12 bytes

    // ****************************************************
    // Meter Codification
    char meterType; // P=polyphase
    char modelType; // 5=COP5, 6=COP6, I=International(Export)
    char modelStatus; // A=ModelA (1st model)
    char configuration; // 0=factory scheme, 1=scheme specified by customer
    int nrOfInputs; // 0..2
    int nrOfOutputs; // 0..4
    String customerId; // see DRG136538
    char rangeDetails; // A=3x230(400)V, 3x40-100A, 4 wire
                       // B=3x230(400)V, 3x/5-6A, 4 wire
                       // C=2x110(400)V, 2x/5-6A, 3 wire
    char certAndSeals; // 0=no certification seals fitted, 1=certification seals fitted
    char memory; // A=16K, B=24K, C=32K, D=40K
    char rs232Port; // 0=not fitted, 1=fitted, 2=fitted+modem, 3=fitted+modem+MMA
    char batteryPower; // B=batt fitted, S=super caps fitted
    char termScrewAccessCover; //0=not fitted, 1=fitted
    char termCover; // A=standard, B=extended

    // ****************************************************
    int formatting;
    // ****************************************************
    String softwareVersionNumber;

    /** Creates a new instance of MeterIdentity */
    public MeterIdentity(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }

    public String toString() {
        return "MeterIdentification: "+getMeterId()+", "+getMeterType()+", "+getModelType()+", "+getModelStatus()+", "+
               getConfiguration()+", "+getNrOfInputs()+", "+getNrOfOutputs()+", "+getCustomerId()+", "+
               getRangeDetails()+", "+getCertAndSeals()+", "+getMemory()+", "+getRs232Port()+", "+
               getBatteryPower()+", "+getTermScrewAccessCover()+", "+getTermCover()+", "+
               getFormatting()+", "+getSoftwareVersionNumber();
     }

    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        setMeterId(new String(ProtocolUtils.getSubArray2(data, 0, 12)));
        setMeterType((char)ProtocolUtils.getSubArray2(data, 12, 1)[0]);
        setModelType((char)ProtocolUtils.getSubArray2(data, 13, 1)[0]);
        setModelStatus((char)ProtocolUtils.getSubArray2(data, 14, 1)[0]);
        setConfiguration((char)ProtocolUtils.getSubArray2(data, 15, 1)[0]);
        setNrOfInputs((char)ProtocolUtils.getSubArray2(data, 16, 1)[0]-0x30);
        setNrOfOutputs((char)ProtocolUtils.getSubArray2(data, 17, 1)[0]-0x30);
        setCustomerId(new String(ProtocolUtils.getSubArray2(data, 18, 2)));
        setRangeDetails((char)ProtocolUtils.getSubArray2(data, 20, 1)[0]);
        setCertAndSeals((char)ProtocolUtils.getSubArray2(data, 21, 1)[0]);
        setMemory((char)ProtocolUtils.getSubArray2(data, 22, 1)[0]);
        setRs232Port((char)ProtocolUtils.getSubArray2(data, 23, 1)[0]);
        setBatteryPower((char)ProtocolUtils.getSubArray2(data, 24, 1)[0]);
        setTermScrewAccessCover((char)ProtocolUtils.getSubArray2(data, 25, 1)[0]);
        setTermCover((char)ProtocolUtils.getSubArray2(data, 26, 1)[0]);
        setFormatting((int)ProtocolUtils.getSubArray2(data, 27, 1)[0]);
        setSoftwareVersionNumber(Integer.toHexString(ProtocolUtils.getInt(data, 28, 1))+"."+Integer.toHexString(ProtocolUtils.getInt(data, 29, 1)));
    }

    /**
     * Getter for property meterId.
     * @return Value of property meterId.
     */
    public java.lang.String getMeterId() {
        return meterId;
    }

    /**
     * Setter for property meterId.
     * @param meterId New value of property meterId.
     */
    public void setMeterId(java.lang.String meterId) {
        this.meterId = meterId;
    }

    /**
     * Getter for property meterType.
     * @return Value of property meterType.
     */
    public char getMeterType() {
        return meterType;
    }

    /**
     * Setter for property meterType.
     * @param meterType New value of property meterType.
     */
    public void setMeterType(char meterType) {
        this.meterType = meterType;
    }

    /**
     * Getter for property modelType.
     * @return Value of property modelType.
     */
    public char getModelType() {
        return modelType;
    }

    /**
     * Setter for property modelType.
     * @param modelType New value of property modelType.
     */
    public void setModelType(char modelType) {
        this.modelType = modelType;
    }

    /**
     * Getter for property modelStatus.
     * @return Value of property modelStatus.
     */
    public char getModelStatus() {
        return modelStatus;
    }

    /**
     * Setter for property modelStatus.
     * @param modelStatus New value of property modelStatus.
     */
    public void setModelStatus(char modelStatus) {
        this.modelStatus = modelStatus;
    }

    /**
     * Getter for property configuration.
     * @return Value of property configuration.
     */
    public char getConfiguration() {
        return configuration;
    }

    /**
     * Setter for property configuration.
     * @param configuration New value of property configuration.
     */
    public void setConfiguration(char configuration) {
        this.configuration = configuration;
    }

    /**
     * Getter for property nrOfInputs.
     * @return Value of property nrOfInputs.
     */
    public int getNrOfInputs() {
        return nrOfInputs;
    }

    /**
     * Setter for property nrOfInputs.
     * @param nrOfInputs New value of property nrOfInputs.
     */
    public void setNrOfInputs(int nrOfInputs) {
        this.nrOfInputs = nrOfInputs;
    }

    /**
     * Getter for property nrOfOutputs.
     * @return Value of property nrOfOutputs.
     */
    public int getNrOfOutputs() {
        return nrOfOutputs;
    }

    /**
     * Setter for property nrOfOutputs.
     * @param nrOfOutputs New value of property nrOfOutputs.
     */
    public void setNrOfOutputs(int nrOfOutputs) {
        this.nrOfOutputs = nrOfOutputs;
    }

    /**
     * Getter for property customerId.
     * @return Value of property customerId.
     */
    public java.lang.String getCustomerId() {
        return customerId;
    }

    /**
     * Setter for property customerId.
     * @param customerId New value of property customerId.
     */
    public void setCustomerId(java.lang.String customerId) {
        this.customerId = customerId;
    }

    /**
     * Getter for property rangeDetails.
     * @return Value of property rangeDetails.
     */
    public char getRangeDetails() {
        return rangeDetails;
    }

    /**
     * Setter for property rangeDetails.
     * @param rangeDetails New value of property rangeDetails.
     */
    public void setRangeDetails(char rangeDetails) {
        this.rangeDetails = rangeDetails;
    }

    /**
     * Getter for property certAndSeals.
     * @return Value of property certAndSeals.
     */
    public char getCertAndSeals() {
        return certAndSeals;
    }

    /**
     * Setter for property certAndSeals.
     * @param certAndSeals New value of property certAndSeals.
     */
    public void setCertAndSeals(char certAndSeals) {
        this.certAndSeals = certAndSeals;
    }

    /**
     * Getter for property memory.
     * @return Value of property memory.
     */
    public char getMemory() {
        return memory;
    }

    /**
     * Setter for property memory.
     * @param memory New value of property memory.
     */
    public void setMemory(char memory) {
        this.memory = memory;
    }

    /**
     * Getter for property rs232Port.
     * @return Value of property rs232Port.
     */
    public char getRs232Port() {
        return rs232Port;
    }

    /**
     * Setter for property rs232Port.
     * @param rs232Port New value of property rs232Port.
     */
    public void setRs232Port(char rs232Port) {
        this.rs232Port = rs232Port;
    }

    /**
     * Getter for property batteryPower.
     * @return Value of property batteryPower.
     */
    public char getBatteryPower() {
        return batteryPower;
    }

    /**
     * Setter for property batteryPower.
     * @param batteryPower New value of property batteryPower.
     */
    public void setBatteryPower(char batteryPower) {
        this.batteryPower = batteryPower;
    }

    /**
     * Getter for property termScrewAccessCover.
     * @return Value of property termScrewAccessCover.
     */
    public char getTermScrewAccessCover() {
        return termScrewAccessCover;
    }

    /**
     * Setter for property termScrewAccessCover.
     * @param termScrewAccessCover New value of property termScrewAccessCover.
     */
    public void setTermScrewAccessCover(char termScrewAccessCover) {
        this.termScrewAccessCover = termScrewAccessCover;
    }

    /**
     * Getter for property termCover.
     * @return Value of property termCover.
     */
    public char getTermCover() {
        return termCover;
    }

    /**
     * Setter for property termCover.
     * @param termCover New value of property termCover.
     */
    public void setTermCover(char termCover) {
        this.termCover = termCover;
    }

    /**
     * Getter for property formatting.
     * @return Value of property formatting.
     */
    public int getFormatting() {
        return formatting;
    }

    /**
     * Setter for property formatting.
     * @param formatting New value of property formatting.
     */
    public void setFormatting(int formatting) {
        this.formatting = formatting;
    }




    /**
     * Setter for property softwareVersionNumber.
     * @param softwareVersionNumber New value of property softwareVersionNumber.
     */
    public void setSoftwareVersionNumber(java.lang.String softwareVersionNumber) {
        this.softwareVersionNumber = softwareVersionNumber;
    }

    /**
     * Getter for property softwareVersionNumber.
     * @return Value of property softwareVersionNumber.
     */
    public java.lang.String getSoftwareVersionNumber() {
        return softwareVersionNumber;
    }

}
