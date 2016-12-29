package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class PseudoBubbleUpTransmissionPeriod extends AbstractParameter {

    public PseudoBubbleUpTransmissionPeriod(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    enum TimeUnit {

        MINUTE1(0, 1),
        HOUR1(1, 60),
        DAY(2, 60 * 24),
        DAY1(3, 60 * 24);

        int minutes;
        int id;

        private TimeUnit(final int id, final int minutes) {
            this.id = id;
            this.minutes = minutes;
        }

        static TimeUnit fromId(int id) throws WaveFlowException {
            for (TimeUnit o : values()) {
                if (o.id == id) {
                    return o;
                }
            }
            throw new WaveFlowException("Invalid timeUnit id [" + WaveflowProtocolUtils.toHexString(id) + "]");
        }

        static int fromSamplingPeriodInMinutes(int samplingPeriodInMinutes) throws WaveFlowException {
            for (TimeUnit o : values()) {
                int multiplier = samplingPeriodInMinutes / o.minutes;
                if (multiplier <= 63) {
                    return (multiplier << 2) | o.id;
                }
            }
            throw new WaveFlowException("Too large the TransmissionPeriodInMinutes [" + samplingPeriodInMinutes + "]. Cannot convert to a valid timeunit-multiplier pair (= transmission period)");
        }
    }

    int transmissionPeriodInMinutes;

    final int getTransmissionPeriodInMinutes() throws IOException {
        return transmissionPeriodInMinutes;
    }

    final void setTransmissionPeriodInMinutes(int transmissionPeriodInMinutes) {
        this.transmissionPeriodInMinutes = transmissionPeriodInMinutes;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.PseudoBubbleUpTransmissionPeriod;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        int temp = WaveflowProtocolUtils.toInt(data[0]);
        int multiplier = temp >> 2;

        if (multiplier == 0) {
            throw new WaveFlowException("Invalid multiplier [" + multiplier + "] in TransmissionPeriod");
        }

        int timeUnitId = temp & 0x03;

        transmissionPeriodInMinutes = TimeUnit.fromId(timeUnitId).minutes * multiplier;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) TimeUnit.fromSamplingPeriodInMinutes(transmissionPeriodInMinutes)};
    }
}