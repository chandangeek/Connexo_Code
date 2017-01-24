package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BackFlowEventTableByFlowRate extends AbstractRadioCommand {

	public BackFlowEventTableByFlowRate(WaveFlow waveFlow) {
		super(waveFlow);
	}

    private List<BackFlowEventByFlowRate> events = new ArrayList<BackFlowEventByFlowRate>();

    public List<BackFlowEventByFlowRate> getEvents() {
        return events;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
		return RadioCommandId.BackFlowEventTable;
	}

	@Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        int inputNumber;
        int volume;
        int detectionDurationInMinutes;
        int backflowDurationInMinutes;
        Date endDate;

        for (int i = 0; i < 4; i++) {
            if (data[offset] == 0xFF) {
                break;
            }
            inputNumber = data[offset] & 0xFF;
            offset++;

            volume = ProtocolTools.getUnsignedIntFromBytes(ProtocolTools.getSubArray(data, offset, offset + 2));
            offset += 2;

            detectionDurationInMinutes = ProtocolTools.getUnsignedIntFromBytes(ProtocolTools.getSubArray(data, offset, offset + 2));
            offset +=2;

            backflowDurationInMinutes = ProtocolTools.getUnsignedIntFromBytes(ProtocolTools.getSubArray(data, offset, offset + 2));
            offset +=2;

            offset +=2;  //Skip 2 unused bytes..

            byte[] dateBytes = ProtocolTools.getSubArray(data, offset, offset + 6);
            if (!isValidDate(dateBytes)) {
                break;       //Stop adding events when encountering an empty date
            }

            endDate = TimeDateRTCParser.parse(data, offset, 6, getWaveFlow().getTimeZone()).getTime();
            offset +=6;

            events.add(new BackFlowEventByFlowRate(getWaveFlow(), inputNumber, volume, detectionDurationInMinutes, backflowDurationInMinutes, endDate));
        }
	}

	@Override
    protected byte[] prepare() throws IOException {
		return new byte[0];         //empty, since the request consists of the parameter ID only
	}

    /**
     * Returns false if the date bytes are all 0x00.
     * @return validity
     */
    private boolean isValidDate(byte[] data) {
        for (byte b : data) {
            if (b != 0) {
                return true;
            }
        }
        return false;
    }

}