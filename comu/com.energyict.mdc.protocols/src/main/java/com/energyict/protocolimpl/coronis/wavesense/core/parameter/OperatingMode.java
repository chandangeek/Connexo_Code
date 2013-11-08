package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

    private static final int PERIODIC_MEASUREMENT = 1;
    private static final int WEEKLY_MEASUREMENT = 2;
    private static final int MONTHLY_MEASUREMENT = 3;
    private static final String CUMMULATIVE_MODE = "cumulative mode";
    private static final String SUCCESSIVE_MODE = "successive mode";

    OperatingMode(WaveSense waveSense) {
        super(waveSense);
    }

    public OperatingMode(WaveSense waveSense, int opMode) {
        super(waveSense);
        this.operationMode = opMode;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.OperationMode;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        this.operationMode = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) operationMode};
    }

    public boolean isSuccessiveThresholdDetectionMode() {
        return (operationMode & 0x40) == 0;
    }

    public boolean isCumulativeThresholdDetectionMode() {
        return (operationMode & 0x40) == 0x40;
    }

    public boolean highThresholdDetectionIsActivated() {
        return (operationMode & 0x10) == 0x10;
    }

    public boolean lowThresholdDetectionIsActivated() {
        return (operationMode & 0x20) == 0x20;
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

    /**
     * 0 = deactivated
     * 1 = time steps
     * 2 = once a week
     * 3 = once a month
     */
    public int dataLoggingSteps() {
        return (operationMode & 0x0C) >> 2;
    }

    public void setDataLoggingStepsToPeriodic() {
        operationMode = operationMode | 0x04;   //mask: 0000.0100 ==> b2 = 1, leave the rest
        operationMode = operationMode & 0xF7;   //mask: 1111.0111 ==> b3 = 0, leave the rest
    }

    public void setDataLoggingToOnceAWeek() {
        operationMode = operationMode | 0x08;   //mask: 0000.1000 ==> b3 = 1, leave the rest
        operationMode = operationMode & 0xFB;   //mask: 1111.1011 ==> b2 = 0, leave the rest
    }

    public void setDataLoggingToOnceAMonth() {
        operationMode = operationMode | 0x08;   //mask: 0000.1000 ==> b3 = 1, leave the rest
        operationMode = operationMode | 0x04;   //mask: 0000.0100 ==> b2 = 1, leave the rest
    }
    
    public void setDataLoggingToStopMode() {
        operationMode = operationMode | 0x02;   //mask: 0000.0010 ==> b1 = 1, leave the rest
    }

    public int dataLoggingStopMode() {
        return (operationMode & 0x02) >> 1;
    }

    public String getDetectionModeDescription() {
        return (isCumulativeThresholdDetectionMode() ? CUMMULATIVE_MODE : SUCCESSIVE_MODE);
    }

    public void stopDataLogging() {
        operationMode = operationMode & 0xF3;   //mask: 1111.0011 ==> b3 = 0, b2 = 0, leave the rest
    }
}