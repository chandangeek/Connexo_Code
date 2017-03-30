/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

    OperatingMode(RTM rtm) {
        super(rtm);
    }

    public OperatingMode(RTM rtm, int opMode) {
        super(rtm);
        this.operationMode = opMode;
        this.mask = 0;
    }

    @Override
    ParameterId getParameterId() {
        return null; //Special case
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        this.operationMode = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return ProtocolTools.getBytesFromInt(operationMode, 2);
    }

    public void setValveCommunicationFaultDetection(int enable) {
        operationMode = operationMode & 0xBFFF;
        this.setMask(~0xBFFF);
        operationMode = operationMode | enable << 14;
    }

    public void setAllDetections(int enable) {
        setEncoderMisReadDetection(enable);
        setExtremeLeakDetection(enable);
        setTamperDetection(enable);
        setBackFlowDetection(enable);
        setEncoderCommunicationFaultDetection(enable);
        setResidualLeakDetection(enable);
        setValveCommunicationFaultDetection(enable);
    }

    public void setNetworkConfiguration(int config) {
        operationMode = operationMode & 0xCFFF;
        this.setMask(~0xCFFF);
        operationMode = operationMode | config << 12;
    }

    public void setBubbleUpManagement(int enable) {
        operationMode = operationMode & 0xF7FF;
        this.setMask(~0xF7FF);
        operationMode = operationMode | enable << 11;
    }

    public void setTouBucketsManagement(int enable) {
        operationMode = operationMode & 0xFBFF;
        this.setMask(~0xFBFF);
        operationMode = operationMode | enable << 10;
    }

    public void setEncoderFilteringAlgorithmManagement(int enable) {
        operationMode = operationMode & 0xFDFF;
        this.setMask(~0xFDFF);
        operationMode = operationMode | enable << 9;
    }

    public void setBackFlowDetection(int enable) {
        operationMode = operationMode & 0xFEFF;
        this.setMask(~0xFEFF);
        operationMode = operationMode | enable << 8;
    }

    public void setNumberOfPorts(int number) {
        operationMode = operationMode & 0xFFFC;
        this.setMask(~0xFFFC);
        operationMode = operationMode | number - 1;
    }

    /**
     * Set the data logging mode
     * @param mode: 0 = Stop, 1 = Periodic time steps, 2 = Weekly and 3 = Monthly
     */
    public void setDataLoggingMode(int mode) {
        operationMode = operationMode & 0xFFF3;
        this.setMask(~0xFFF3);
        operationMode = operationMode | mode << 2;
    }

    public int getDataLoggingMode() {
        return (operationMode & 0x0C) >> 2;
    }

    public String getLoggingDescription() {
        switch (getDataLoggingMode()) {
            case 0: return "Data logging is stopped";
            case 1: return "Logging in periodic time steps";
            case 2: return "Weekly data logging";
            case 3: return "Monthly data logging";
            default: return "";
        }
    }

    public void setTamperDetection(int enable) {
        operationMode = operationMode & 0xFFEF;
        this.setMask(~0xFFEF);
        operationMode = operationMode | enable << 4;
    }

    public void setEncoderCommunicationFaultDetection(int enable) {
        operationMode = operationMode & 0xFFEF;
        this.setMask(~0xFFEF);
        operationMode = operationMode | enable << 4;
    }

    public void setResidualLeakDetection(int enable) {
        operationMode = operationMode & 0xFFDF;
        this.setMask(~0xFFDF);
        operationMode = operationMode | enable << 5;
    }

    public void setExtremeLeakDetection(int enable) {
        operationMode = operationMode & 0xFFBF;
        this.setMask(~0xFFBF);
        operationMode = operationMode | enable << 6;
    }

    public void setEncoderMisReadDetection(int enable) {
        operationMode = operationMode & 0xFF7F;
        this.setMask(~0xFF7F);
        operationMode = operationMode | enable << 7;
    }

    public int readValveCommunicationFaultDetection() {
        return (operationMode & 0x4000) >> 14;
    }

    public int readNetworkConfiguration() {
        return (operationMode & 0x3000) >> 12;
    }

    public int readBubbleUpManagement() {
        return (operationMode & 0x0800) >> 11;
    }

    public int readTouBucketsManagement() {
        return (operationMode & 0x0400) >> 10;
    }

    public int readEncoderFilteringAlgorithmManagement() {
        return (operationMode & 0x0200) >> 9;
    }

    public int readBackFlowDetection() {
        return (operationMode & 0x0100) >> 8;
    }

    public int readNumberOfPorts() {
        return (operationMode & 0x0003) + 1;
    }

    public int readDataLoggingMode() {
        return (operationMode & 0x000C) >> 2;
    }

    public boolean isPeriodicLogging() {
        return readDataLoggingMode() == 1;
    }

    public boolean isWeeklyLogging() {
        return readDataLoggingMode() == 2;
    }

    public boolean isMonthlyLogging() {
        return readDataLoggingMode() == 3;
    }

    public int readTamperDetection() {
        return (operationMode & 0x0010) >> 4;
    }

    public int readEncoderCommunicationFaultDetection() {
        return (operationMode & 0x0010) >> 4;
    }

    public int readResidualLeakDetection() {
        return (operationMode & 0x0020) >> 5;
    }

    public int readExtremeLeakDetection() {
        return (operationMode & 0x0040) >> 6;
    }

    public int readEncoderMisReadDetection() {
        return (operationMode & 0x0080) >> 7;
    }
}