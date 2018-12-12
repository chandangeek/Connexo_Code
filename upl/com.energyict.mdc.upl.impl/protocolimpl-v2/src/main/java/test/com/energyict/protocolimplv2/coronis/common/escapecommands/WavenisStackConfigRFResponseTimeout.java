package test.com.energyict.protocolimplv2.coronis.common.escapecommands;

import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavenisStackConfigRFResponseTimeout extends AbstractEscapeCommand {

    public WavenisStackConfigRFResponseTimeout(WaveFlowConnect waveFlowConnect, int timeout) {
        super(waveFlowConnect);
        this.timeout = timeout;
    }

    private int timeout;

    @Override
    public EscapeCommandId getEscapeCommandId() {
        return EscapeCommandId.WAVENIS_CONFIG_RF_TIMEOUT;
    }

    @Override
    public void parse(byte[] data){
    }

    @Override
    public byte[] prepare(){
        return ProtocolTools.getBytesFromInt(timeout, 4);
    }
}