package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeakageEventTable extends AbstractRadioCommand {

    LeakageEvent[] leakageEvents = new LeakageEvent[5];

    LeakageEventTable(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public final LeakageEvent[] getLeakageEvents() {
        return leakageEvents;
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<>();
        EventStatusAndDescription translator = new EventStatusAndDescription();
        for (LeakageEvent event : leakageEvents) {
            if (event != null) {
                meterEvents.add(new MeterEvent(event.getDate(), 0,  translator.getProtocolCodeForLeakage(event.getStatusDescription(), event.getLeakageType(), event.getCorrespondingInputChannel()), event.getDescription()));
            }
        }
        return meterEvents;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.LeakageEventTable;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            dais.skipBytes(23);          //Skip the generic header
            for (int i = 0; i < leakageEvents.length; i++) {
                int status = WaveflowProtocolUtils.toInt(dais.readByte());
                int consumptionRate = WaveflowProtocolUtils.toInt(dais.readShort());
                byte[] timestamp = new byte[7];
                dais.read(timestamp);
                if (isAllZeroes(timestamp)) {
                    break;
                }
                leakageEvents[i] = new LeakageEvent(status, consumptionRate, timestamp, getRTM());
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private boolean isAllZeroes(byte[] timestamp) {
        for (byte b : timestamp) {
            if ((b & 0xFF) != 0x00) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}