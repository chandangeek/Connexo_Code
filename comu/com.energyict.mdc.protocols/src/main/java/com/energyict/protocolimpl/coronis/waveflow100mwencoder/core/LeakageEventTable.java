package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.EventStatusAndDescription;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.LeakageEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

public class LeakageEventTable extends AbstractRadioCommand {

    public class LeakEvent {


        /**
         * indicates the event type (occurrence or disappearance) and the corresponding Port.
         * Bit 7 Bit 6 Bit 5 Bit 4 Bit 3 Bit 2 Bit 1 Bit 0
         * Corresponding Port
         * bit7..6: 00 : Port A 01 : Port B
         * bit5..2: reserved
         * bit1: Leak type 0 : Extreme leak	1 : Residual leak
         * bit0: Event Type	0 : disappearance 1 : occurence
         */
        int status;

        /**
         *
         */
        int consumptionRate;

        /**
         * leakage event timestamp
         */
        Date date = null;

        /**
         * valid or not
         */
        boolean valid = false;


        public final boolean isValid() {
            return valid;
        }

        private LeakEvent(int status, int consumptionRate, byte[] timestamp) throws IOException {
            this.status = status;
            this.consumptionRate = consumptionRate;

            for (byte b : timestamp) {
                if (b != 0) {
                    valid = true;
                    this.date = TimeDateRTCParser.parse(timestamp, getWaveFlow100mW().getTimeZone()).getTime();
                    break;
                }
            }
        }

        public final int getStatus() {
            return status;
        }

        public final int getConsumptionRate() {
            return consumptionRate;
        }

        public final Date getDate() {
            return date;
        }

        public String getEventDescription() {
            return getStartDescription() + " of " + getTypeDescription() + " leak on port " + getPortDescription();
        }

        private String getStartDescription() {
            return ((status & 0x01) == 0x01) ? LeakageEvent.START : LeakageEvent.END;
        }

        private String getTypeDescription() {
            return ((status & 0x02) == 0x02) ? LeakageEvent.LEAKAGETYPE_RESIDUAL : LeakageEvent.LEAKAGETYPE_EXTREME;
        }

        private String getPortDescription() {
            return ((status & 0x40) == 0x40) ? LeakageEvent.B : LeakageEvent.A;
        }

        public int getDeviceCode() {
            EventStatusAndDescription eventStatus = new EventStatusAndDescription(0);
            return eventStatus.getProtocolCodeForLeakage(getStartDescription(), getTypeDescription(), getPortDescription());
        }
    }

    LeakEvent[] leakageEvents = new LeakEvent[5];

    public final LeakEvent[] getLeakageEvents() {
        return leakageEvents;
    }

    LeakageEventTable(WaveFlow100mW waveFlow100mW) {
        super(waveFlow100mW);
    }

    @Override
    EncoderRadioCommandId getEncoderRadioCommandId() {
        return EncoderRadioCommandId.LeakageEventTable;
    }

    @Override
    void parse(byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            for (int i = 0; i < leakageEvents.length; i++) {
                int status = WaveflowProtocolUtils.toInt(dais.readByte());
                int consumptionRate = WaveflowProtocolUtils.toInt(dais.readShort());
                byte[] timestamp = new byte[7];
                dais.read(timestamp);
                leakageEvents[i] = new LeakEvent(status, consumptionRate, timestamp);
            }
        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    getWaveFlow100mW().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    @Override
    byte[] prepare() throws IOException {
        return new byte[0];
    }

}
