package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class TransmissionPeriod extends AbstractParameter {

    public TransmissionPeriod(WaveFlow waveFlow) {
        super(waveFlow);
    }

    enum TimeUnit {

        MINUTE1(0, 1),
        HOUR1(1, 60),
        DAY1(2, 60 * 24);

        int minutes;
        int id;

        private TimeUnit(final int id, final int minutes) {
            this.id = id;
            this.minutes = minutes;
        }

        static TimeUnit fromId(int id) {
            for (TimeUnit o : values()) {
                if (o.id == id) {
                    return o;
                }
            }
            throw createWaveFlowException("Invalid timeUnit id [" + WaveflowProtocolUtils.toHexString(id) + "]");
        }

        static int fromSamplingPeriodInMinutes(int samplingPeriodInMinutes) {
            for (TimeUnit o : values()) {
                int multiplier = samplingPeriodInMinutes / o.minutes;
                if (multiplier <= 63) {
                    return (multiplier << 2) | o.id;
                }
            }
            throw createWaveFlowException("Too large the TransmissionPeriodInMinutes [" + samplingPeriodInMinutes + "]. Cannot convert to a valid timeunit-multiplier pair (= transmission period)");
        }
    }

    int transmissionPeriodInMinutes;

    final int getTransmissionPeriodInMinutes() {
        return transmissionPeriodInMinutes;
    }

    final void setTransmissionPeriodInMinutes(int transmissionPeriodInMinutes) {
        this.transmissionPeriodInMinutes = transmissionPeriodInMinutes;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.TransmissionPeriod;
    }

    @Override
    public void parse(byte[] data) {
        int temp = WaveflowProtocolUtils.toInt(data[0]);
        int multiplier = temp >> 2;

        if (multiplier == 0) {
            throw createWaveFlowException("Invalid multiplier [" + multiplier + "] in TransmissionPeriod");
        }

        int timeUnitId = temp & 0x03;

        transmissionPeriodInMinutes = TimeUnit.fromId(timeUnitId).minutes * multiplier;

        //Special case to define "once a day", see documentation page 105.
        if (temp == 0x02) {
            transmissionPeriodInMinutes = TimeUnit.fromId(2).minutes;
        }
        if (temp == 0x01) {
            transmissionPeriodInMinutes = TimeUnit.fromId(1).minutes;
        }
        if (temp == 0x00) {
            transmissionPeriodInMinutes = TimeUnit.fromId(0).minutes;
        }
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) TimeUnit.fromSamplingPeriodInMinutes(transmissionPeriodInMinutes)};
    }
}