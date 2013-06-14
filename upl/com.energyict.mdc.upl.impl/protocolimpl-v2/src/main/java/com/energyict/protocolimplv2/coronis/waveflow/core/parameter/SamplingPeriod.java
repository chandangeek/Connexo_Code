package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class SamplingPeriod extends AbstractParameter {

    public SamplingPeriod(WaveFlow waveFlow) {
        super(waveFlow);
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

        static TimeUnit fromId(int id) {
            for (TimeUnit o : values()) {
                if (o.id == id) {
                    return o;
                }
            }
            throw createWaveFlowException("Invalid timeUnit id [" + WaveflowProtocolUtils.toHexString(id) + "]");
        }

        /**
         * return the sampling period byte to be programmed in the waveflow module
         *
         * @param samplingPeriodInSeconds
         * @return sampling period to be programmed in the waveflow module
         * @throws com.energyict.protocolimpl.coronis.core.WaveFlowException
         *
         */
        static int fromSamplingPeriodInSeconds(int samplingPeriodInSeconds) {
            for (TimeUnit o : values()) {
                int multiplier = samplingPeriodInSeconds / o.seconds;
                if (multiplier <= 63) {
                    return (multiplier << 2) | o.id;
                }
            }
            throw createWaveFlowException("Too large the samplingPeriodInSeconds [" + samplingPeriodInSeconds + "]. Cannot convert to a valid timeunit-multiplier pair (sampling period)");
        }
    }

    int samplingPeriodInSeconds;

    public final int getSamplingPeriodInSeconds() {
        return samplingPeriodInSeconds;
    }

    final void setSamplingPeriodInSeconds(int samplingPeriodInSeconds) {
        this.samplingPeriodInSeconds = samplingPeriodInSeconds;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.SamplingPeriod;
    }


    @Override
    public void parse(byte[] data) {
        int temp = WaveflowProtocolUtils.toInt(data[0]);
        int multiplier = temp >> 2;

        if (multiplier == 0) {
            throw createWaveFlowException("Invalid multiplier [" + multiplier + "] in SamplingPeriod");
        }

        int timeUnitId = temp & 0x03;

        samplingPeriodInSeconds = TimeUnit.fromId(timeUnitId).seconds * multiplier;
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) TimeUnit.fromSamplingPeriodInSeconds(samplingPeriodInSeconds)};
    }

}