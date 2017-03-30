/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.SamplingPeriod;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class DataloggingTable extends AbstractRadioCommand {

    /**
     * Invoking this object results in the last 24 meter indexes (profile data).
     * The ExtendedDataloggingTable reads up to 525 indexes.
     */
    public DataloggingTable(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private SamplingPeriod dataloggingMeasurementPeriod;
    private int channels;
    private Date lastLoggedIndexDate;
    private Long[] profileDataA;
    private Long[] profileDataB;
    private Long[] profileDataC;
    private Long[] profileDataD;

    public SamplingPeriod getDataloggingMeasurementPeriod() {
        return dataloggingMeasurementPeriod;
    }

    /**
     * @param channels: 1 = channel A, 12 = channels A and B, 3 = channel C, 34 = channels C and D
     */
    public void setChannels(int channels) {
        this.channels = channels;
    }

    public Long[] getProfileDataA() {
        return profileDataA;
    }

    public Long[] getProfileData(int index) {
        switch (index) {
            case 0: return profileDataA;
            case 1: return profileDataB;
            case 2: return profileDataC;
            case 3: return profileDataD;
        }
        return new Long[0];
    }

    public Long[] getProfileDataB() {
        return profileDataB;
    }

    public Long[] getProfileDataC() {
        return profileDataC;
    }

    public Long[] getProfileDataD() {
        return profileDataD;
    }

    public Date getLastLoggedIndexDate() {
        return lastLoggedIndexDate;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        if (channels == 1) {
            return RadioCommandId.DataloggingTableAB;
        }
        if (channels == 12) {
            return RadioCommandId.DataloggingTableAB;
        }
        if (channels == 3) {
            return RadioCommandId.DataloggingTableCD;
        }
        if (channels == 34) {
            return RadioCommandId.DataloggingTableCD;
        }
        return RadioCommandId.DataloggingTableAB;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        int offset = 0;

        operationMode = data[offset] & 0xFF;
        getWaveFlow().getParameterFactory().setOperatingMode(operationMode);
        offset++;

        applicationStatus = data[offset] & 0xFF;
        getWaveFlow().getParameterFactory().setApplicationStatus(applicationStatus);
        offset++;

        if (channels == 1) {
            profileDataA = new Long[24];
            for (int i = 0; i < 24; i++) {
                profileDataA[i] = (long) ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
        } else if (channels == 3) {
            profileDataC = new Long[12];
            for (int i = 0; i < 12; i++) {
                profileDataC[i] = (long) ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
            offset += 48; //Unused bytes in this case
        } else if (channels == 12) {
            profileDataA = new Long[12];
            for (int i = 0; i < 12; i++) {
                profileDataA[i] = (long) ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
            profileDataB = new Long[12];
            for (int i = 0; i < 12; i++) {
                profileDataB[i] = (long) ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
        } else if (channels == 34) {
            profileDataC = new Long[12];
            for (int i = 0; i < 12; i++) {
                profileDataC[i] = (long) ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
            profileDataD = new Long[12];
            for (int i = 0; i < 12; i++) {
                profileDataD[i] = (long) ProtocolTools.getUnsignedIntFromBytes(data, offset, 4);
                offset += 4;
            }
        }
        byte[] dateBytes = ProtocolTools.getSubArray(data, offset, offset + 6);
        TimeZone timeZone = getWaveFlow().getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        lastLoggedIndexDate = TimeDateRTCParser.parse(dateBytes, timeZone).getTime();
        offset += 6;

        dataloggingMeasurementPeriod = new SamplingPeriod(getWaveFlow());
        dataloggingMeasurementPeriod.parse(WaveflowProtocolUtils.getSubArray(data, offset, 1));
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}