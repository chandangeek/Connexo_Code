package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 28-feb-2011
 * Time: 11:12:46
 */
public class ReedFaultDetectionDate extends AbstractParameter {

    private int inputChannel;
    private Date eventDate;

    ReedFaultDetectionDate(WaveFlow waveFlow) {
        super(waveFlow);
    }

    ReedFaultDetectionDate(WaveFlow waveFlow, int inputChannel) {
        super(waveFlow);
        this.inputChannel = inputChannel;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public int getInputChannel() {
        return inputChannel;
    }

    @Override
    protected ParameterId getParameterId() {
        switch (inputChannel) {
            case 0: return ParameterId.ReedFaultDetectionDateInputA;
            case 1: return ParameterId.ReedFaultDetectionDateInputB;
            default: return ParameterId.ReedFaultDetectionDateInputA;
        }
    }

    @Override
    protected void parse(byte[] data) {
        eventDate = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }
}
