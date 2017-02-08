package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

/**
 * Copyrights EnergyICT
 * Date: 5-apr-2011
 * Time: 14:13:24
 */
public class ElapsedDays extends AbstractParameter {

    ElapsedDays(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int days = 0;

    public int getDays() {
        return days;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ElapsedDays;
    }

    @Override
    protected void parse(byte[] data) {
        days = ProtocolTools.getUnsignedIntFromBytes(data);
    }

    @Override
    protected byte[] prepare() {
        throw CodingException.unsupportedMethod(getClass(), "prepare");
    }
}