/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class MeasurementPeriod extends AbstractParameter {

    public MeasurementPeriod(WaveTherm waveTherm) {
        super(waveTherm);
    }

    enum TimeUnit {

        MINUTE1(0, 1 * 60),
        MINUTE5(1, 5 * 60),
        MINUTE15(2, 15 * 60),
        MINUTE30(3, 30 * 60);

        int seconds;
        int id;

        private TimeUnit(final int id, final int seconds) {
            this.id = id;
            this.seconds = seconds;
        }

        static TimeUnit fromId(int id) throws WaveFlowException {
            for (TimeUnit o : values()) {
                if (o.id == id) {
                    return o;
                }
            }
            throw new WaveFlowException("Invalid timeUnit id [" + WaveflowProtocolUtils.toHexString(id) + "]");
        }

        /**
         * return the sampling period to be programmed in the waveflow module
         *
         * @param samplingPeriodInSeconds
         * @return sampling period to be programmed in the waveflow module
         * @throws com.energyict.protocolimpl.coronis.core.WaveFlowException
         *
         */
        static int fromSamplingPeriodInSeconds(int samplingPeriodInSeconds) throws WaveFlowException {
            for (TimeUnit o : values()) {
                int multiplier = samplingPeriodInSeconds / o.seconds;
                if (multiplier <= 63) {
                    return (multiplier << 2) | o.id;
                }
            }
            throw new WaveFlowException("Too large the samplingPeriodInSeconds [" + samplingPeriodInSeconds + "]. Cannot convert to a valid timeunit-multiplier pair (sampling period)");
        }
    }

    int samplingPeriodInSeconds;

    final int getSamplingPeriodInSeconds() {
        return samplingPeriodInSeconds;
    }

    final void setSamplingPeriodInSeconds(int samplingPeriodInSeconds) {
        this.samplingPeriodInSeconds = samplingPeriodInSeconds;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.SamplingPeriod;
    }


    @Override
    public void parse(byte[] data) throws IOException {
        int samplingByte = WaveflowProtocolUtils.toInt(data[0]);
        int multiplier = samplingByte >> 2;
        if (multiplier == 0) {
            throw new WaveFlowException("Invalid multiplier [" + multiplier + "] in SamplingPeriod");
        }
        int timeUnitId = samplingByte & 0x03;
        samplingPeriodInSeconds = TimeUnit.fromId(timeUnitId).seconds * multiplier;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) TimeUnit.fromSamplingPeriodInSeconds(samplingPeriodInSeconds)};
    }
}