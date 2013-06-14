package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class LeakageDetectionMeasurementStep extends AbstractParameter {

    /**
     * This is the number of minutes that defines one time step in the leakage detection.
     */
    int measurementStep;

    LeakageDetectionMeasurementStep(WaveFlow waveFlow) {
        super(waveFlow);
    }

    final int getMeasurementStep() {
        return measurementStep;
    }

    final void setMeasurementStep(int measurementStep) {
        this.measurementStep = measurementStep;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.LeakageMeasurementStep;
    }

    @Override
    protected void parse(byte[] data) {
        measurementStep = ProtocolTools.getIntFromBytes(data, 0, 1);
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) measurementStep};
    }
}