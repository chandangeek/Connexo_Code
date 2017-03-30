/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ErrorCodesCommand extends AbstractCommand {

    /*
    DX ERROR Codebyte
    0 Low battery
    1 Unprogrammed
    2 Memory failure
    3 Demand overflow
    4 Stuck switch
    5 Unsafe power failure
    6 Reverse rotation
    7 Not used

    RX Error code 1 & mask 1
    0 Low battery
    1 Unprogrammed
    2 Memory failure
    3 Demand overflow
    4 Stuck switch
    5 Unsafe power failure
    6 Not used
    7 Not used

    RX Error code 2 & mask 2
    0 Low battery
    1 DSP overflow error
    2 Phase error
    3 Demand overflow
    4 Stuck switch
    5 Measurement diag.
    6 Not used
    7 Not used

    Memory failure
    0 EEPROM checksum 1
    1 EEPROM checksum 2
    2 EEPROM checksum 3
    3 A/D setpoints
    4 Load profile parity
    5 ROM checksum
    6 Serial EEPROM
    7 SRAM error or missing

    Phase error
    0 Phase C out
    1 Phase B out
    2 50/60Hz missing
    3 Phase A out (not prior to V3.00)
    4..6 Not used
    7 Unknown service

    Measurement diagnostics failure
    0 ASIC not initialized
    1 DSP initialization error
    2 DSP comm. error
    3 ASIC data overrun
    4 KW Subinterval Sftwr Accum Overflow
    5 KM Subinterval Sftwr Accum Overflow
    6 DSP comm. timeout
    7 Out of calibration
     */

    private int errorCode1;
    private int errorCode2;
    private int errorMask1;
    private int errorMask2;
    private int memoryFailure;
    private int phaseError;
    private int measurementDiagnosticFailure;



    /** Creates a new instance of TemplateCommand */
    public ErrorCodesCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ErrorCodesCommand:\n");
        strBuff.append("   errorCode1=0x"+Integer.toHexString(getErrorCode1())+"\n");
        strBuff.append("   errorCode2=0x"+Integer.toHexString(getErrorCode2())+"\n");
        strBuff.append("   errorMask1=0x"+Integer.toHexString(getErrorMask1())+"\n");
        strBuff.append("   errorMask2=0x"+Integer.toHexString(getErrorMask2())+"\n");
        strBuff.append("   measurementDiagnosticFailure=0x"+Integer.toHexString(getMeasurementDiagnosticFailure())+"\n");
        strBuff.append("   memoryFailure=0x"+Integer.toHexString(getMemoryFailure())+"\n");
        strBuff.append("   phaseError=0x"+Integer.toHexString(getPhaseError())+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)0x83,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX()) {
            setErrorCode1(ProtocolUtils.getInt(data,0,1));
        }
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            setErrorCode1(ProtocolUtils.getInt(data,0,1));
            setErrorCode2(ProtocolUtils.getInt(data,1,1));
            setErrorMask1(ProtocolUtils.getInt(data,2,1));
            setErrorMask2(ProtocolUtils.getInt(data,3,1));
            setMemoryFailure(ProtocolUtils.getInt(data,4,1));
            setPhaseError(ProtocolUtils.getInt(data,5,1));
            setMeasurementDiagnosticFailure(ProtocolUtils.getInt(data,6,1));
        }
    }

    public int getErrorCode1() {
        return errorCode1;
    }

    private void setErrorCode1(int errorCode1) {
        this.errorCode1 = errorCode1;
    }

    public int getErrorCode2() {
        return errorCode2;
    }

    private void setErrorCode2(int errorCode2) {
        this.errorCode2 = errorCode2;
    }

    public int getErrorMask1() {
        return errorMask1;
    }

    private void setErrorMask1(int errorMask1) {
        this.errorMask1 = errorMask1;
    }

    public int getErrorMask2() {
        return errorMask2;
    }

    private void setErrorMask2(int errorMask2) {
        this.errorMask2 = errorMask2;
    }

    public int getMemoryFailure() {
        return memoryFailure;
    }

    private void setMemoryFailure(int memoryFailure) {
        this.memoryFailure = memoryFailure;
    }

    public int getPhaseError() {
        return phaseError;
    }

    private void setPhaseError(int phaseError) {
        this.phaseError = phaseError;
    }

    public int getMeasurementDiagnosticFailure() {
        return measurementDiagnosticFailure;
    }

    private void setMeasurementDiagnosticFailure(int measurementDiagnosticFailure) {
        this.measurementDiagnosticFailure = measurementDiagnosticFailure;
    }

    public final int MEMORY_FAILURE = 0x04; // error code 1
    public final int PHASE_FAILURE = 0x04; // error code 2
    public final int MEASUREMENT_DIAGNOSTIC_FAILURE = 0x20; // error code 2
    public final int REVERSE_ROTATION = 0x40; // error code 1

    public int getIntervalStateBits() throws IOException {
        int eiState=0;
        if (((getErrorMask1()&MEMORY_FAILURE) == MEMORY_FAILURE) && ((getErrorCode1()&MEMORY_FAILURE) == MEMORY_FAILURE)) {
            if ((getMemoryFailure() & 0x10)==0x10)
                eiState |= IntervalStateBits.CORRUPTED;
        }

        if (((getErrorMask2()&PHASE_FAILURE) == PHASE_FAILURE) && ((getErrorCode2()&PHASE_FAILURE) == PHASE_FAILURE)) {
            if (((getPhaseError() & 0x1)==0x1) || ((getPhaseError() & 0x2)==0x2) || ((getPhaseError() & 0x8)==0x8))
                eiState |= IntervalStateBits.PHASEFAILURE;
        }

        if (((getErrorMask2()&MEASUREMENT_DIAGNOSTIC_FAILURE) == MEASUREMENT_DIAGNOSTIC_FAILURE) && ((getErrorCode2()&MEASUREMENT_DIAGNOSTIC_FAILURE) == MEASUREMENT_DIAGNOSTIC_FAILURE)) {
            if ((getMemoryFailure() & 0x10)==0x10)
                eiState |= IntervalStateBits.CORRUPTED;
        }

        if (getCommandFactory().getFirmwareVersionCommand().isDX()) {
            if ((getErrorMask1()&REVERSE_ROTATION) == REVERSE_ROTATION)
                eiState |= IntervalStateBits.REVERSERUN;
        }
        return eiState;

    }
}
