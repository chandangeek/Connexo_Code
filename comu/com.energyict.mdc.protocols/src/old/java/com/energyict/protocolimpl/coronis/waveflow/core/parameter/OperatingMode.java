/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

    private static final int PERIODIC_MEASUREMENT = 1;
    private static final int WEEKLY_MEASUREMENT = 2;
    private static final int MONTHLY_MEASUREMENT = 3;
    private ParameterId parameterId = ParameterId.OperationMode;

    public OperatingMode(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public OperatingMode(WaveFlow waveFlow, int opMode) {
        super(waveFlow);
        this.operationMode = opMode;
    }


    public boolean isPeriodicMeasurement() {
        return (dataLoggingSteps() == PERIODIC_MEASUREMENT);
    }

    public boolean isWeeklyMeasurement() {
        return (dataLoggingSteps() == WEEKLY_MEASUREMENT);
    }

    public boolean isMonthlyMeasurement() {
        return (dataLoggingSteps() == MONTHLY_MEASUREMENT);
    }

    public int dataLoggingSteps() {
        return (operationMode & 0x0C) >> 2;
    }

    public String getLoggingDescription() {
        switch (dataLoggingSteps()) {
            case 0: return "Data logging is stopped";
            case 1: return "Logging in periodic time steps";
            case 2: return "Weekly data logging";
            case 3: return "Monthly data logging";
            default: return "";            
        }
    }

    public void setDataLoggingStepsToPeriodic() {
        operationMode = operationMode | 0x04;   //mask: 0000.0100 ==> b2 = 1, leave the rest
        operationMode = operationMode & 0xF7;   //mask: 1111.0111 ==> b3 = 0, leave the rest
    }

    public void setDataLoggingToOnceAWeek() {
        operationMode = operationMode & 0xFB;   //mask: 1111.1011 ==> b2 = 0, leave the rest
        operationMode = operationMode | 0x08;   //mask: 0000.1000 ==> b3 = 1, leave the rest
    }

    public void setDataLoggingToOnceAMonth() {
        operationMode = operationMode | 0x04;   //mask: 0000.0100 ==> b2 = 1, leave the rest
        operationMode = operationMode | 0x08;   //mask: 0000.1000 ==> b3 = 1, leave the rest
    }

    public boolean reedFaultDetectionIsEnabled() {
        return ((operationMode >> 7) == 0x01);
    }

    public boolean wireCutDetectionIsEnabled() {
        return (((operationMode >> 4) & 0x01) == 0x01);
    }

    public boolean residualLeakDetectionIsEnabled() {
        return (((operationMode >> 5) & 0x01) == 0x01);
    }

    public boolean extremeLeakDetectionIsEnabled() {
        return (((operationMode >> 6) & 0x01) == 0x01);
    }

    public int getNumberOfInputsUsed() {
        return ((operationMode & 0x03) + 1); //b1 - b0 contain info about the number of inputs used. (min: 1, max: 4)
    }

    @Override
    protected ParameterId getParameterId() {
        return parameterId;
    }

    public void setParameterId(ParameterId parameterId) {
        this.parameterId = parameterId;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        this.operationMode = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) operationMode};
    }

    public void setNumberOfInputsUsed(int number) {
        operationMode = operationMode & 0xFC;           //Set b0 and b1 to zero
        operationMode = operationMode | (number - 1);   //Give b0 and b1 the appropriate value
    }

    public void stopDataLogging() {
        operationMode = operationMode & 0xF3;   //mask: 1111.0011 ==> b3 = 0 and b2 = 0, leave the rest
    }
}