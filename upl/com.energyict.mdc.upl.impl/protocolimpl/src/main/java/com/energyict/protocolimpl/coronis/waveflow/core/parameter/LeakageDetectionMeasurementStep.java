package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

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
    protected void parse(byte[] data) throws IOException {
        measurementStep = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) measurementStep};
    }
}