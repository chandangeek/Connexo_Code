/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class LeakageEventTable extends AbstractRadioCommand {


    LeakageEvent[] leakageEvents = new LeakageEvent[5];

    final public LeakageEvent[] getLeakageEvents() {
        return leakageEvents;
    }

    LeakageEventTable(WaveFlow waveFlow) {
        super(waveFlow);
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
            for (int i = 0; i < leakageEvents.length; i++) {
                int status = WaveflowProtocolUtils.toInt(dais.readByte());
                int consumptionRate = WaveflowProtocolUtils.toInt(dais.readShort());
                byte[] timestamp = new byte[6];
                dais.read(timestamp);
                leakageEvents[i] = new LeakageEvent(status, consumptionRate, timestamp, getWaveFlow());
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getWaveFlow().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}