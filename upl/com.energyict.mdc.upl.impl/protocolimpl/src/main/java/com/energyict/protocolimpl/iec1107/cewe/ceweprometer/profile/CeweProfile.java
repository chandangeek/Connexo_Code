package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.FirmwareVersion;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CeweDateFormats;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 11/05/11
 * Time: 10:16
 */
public class CeweProfile {

    public static final FirmwareVersion FW_2_1_0 = new FirmwareVersion("2.1.0");

    private final CewePrometer cewePrometer;

    public CeweProfile(CewePrometer cewePrometer) {
        this.cewePrometer = cewePrometer;
    }

    /**
     * Turn a response into a ProfileData object.  A response consists of a series of intervals.
     *
     * @param rawProfileData
     * @return
     * @throws java.io.IOException
     */
    public ProfileData toProfileData(String rawProfileData) throws IOException {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        List<ProRegister> profileAsRegisters = splitIntervals(rawProfileData);
        Date previous = null;
        for (ProRegister proRegister : profileAsRegisters) {
            previous = add(pd, proRegister, previous);
        }
        return pd;
    }

    /**
     * Add a single ProRegister (=interval) to the ProfileData object
     *
     * @param profileData
     * @param register
     * @param previousDate
     * @return
     * @throws IOException
     */
    private Date add(ProfileData profileData, ProRegister register, Date previousDate) throws IOException {
        Date date = register.asShortDate(0);
        Date tMinus1Hour = new Date(date.getTime() - 3600L * 1000L);
        Date intervalDate = date;

        if (!isDst(date) && isDst(tMinus1Hour)) {
            /* in doubt */
            Date tMinusInterval = new Date(date.getTime() - cewePrometer.getProfileInterval() * 1000);
            if (previousDate != null && tMinusInterval.after(previousDate)) {
                intervalDate = tMinus1Hour;
            }
        }

        int pStatus = register.asInt(1);

        IntervalData id = new IntervalData(intervalDate);
        id.setEiStatus(toIntervalStateBitToEIStatus(pStatus));
        id.setProtocolStatus(pStatus);

        for (int idx = 2; idx < register.size(); idx++) {
            id.addValue(register.asDouble(idx));
        }

        profileData.addInterval(id);
        return intervalDate;
    }

    private boolean isDst(Date date) {
        return getTimeZone().inDaylightTime(date);
    }

    private TimeZone getTimeZone() {
        return cewePrometer.getTimeZone();
    }

    int toIntervalStateBitToEIStatus(int tag) {
        int ei = 0;
        switch (tag) {
            // time set
            case 0x01:
                ei |= IntervalStateBits.SHORTLONG;
                break;
            // disturbed
            case 0x02:
                ei |= IntervalStateBits.OTHER;
                break;
            // user alarm
            case 0x04:
                ei |= IntervalStateBits.OTHER;
                break;
            // parameter data change
            case 0x08:
                ei |= IntervalStateBits.CONFIGURATIONCHANGE;
                break;
            // reverse running
            case 0x0F:
                ei |= IntervalStateBits.REVERSERUN;
                break;
            // meter clock in DST
            case 0x10:
                ei |= IntervalStateBits.OTHER;
                break;
            // voltage lost/missing
            case 0x40:
                ei |= IntervalStateBits.OTHER;
                break;
            // corrupted
            case 0x80:
                ei |= IntervalStateBits.CORRUPTED;
                break;
        }
        return ei;
    }

    private Date getNextDateToRequestFromResponse(String response) throws IOException {
        List<ProRegister> proRegisters = splitIntervals(response);
        ProRegister lastInterval = proRegisters.get(proRegisters.size() - 1);
        Date lastIntervalDate = lastInterval.asDate(getDateFormats().getShortDateFormat());
        Date nextInterval = new Date(lastIntervalDate.getTime() + cewePrometer.getProfileInterval() * 1000);
        return nextInterval;
    }

    /**
     * Build ChannelInfo based on LogChannelConfig
     *
     * @return
     * @throws IOException
     */
    private List<ChannelInfo> getChannelInfos() throws IOException {
        int channelCount = cewePrometer.getNumberOfChannels();
        FirmwareVersion fw = cewePrometer.getFirmwareVersionObject();
        if (fw.before(FW_2_1_0)) {
            String rawData = cewePrometer.getRegisters().getrLogChannelConfigOld()[cewePrometer.getPLogger()].getRawData();
            return ChannelConfigurationParser.toChannelInfoOldFw(rawData, channelCount);
        } else {
            String[] rawData = new String[channelCount];
            for (int channelIndex = 0; channelIndex < rawData.length; channelIndex++) {
                rawData[channelIndex] = cewePrometer.getRegisters().getrLogChannelConfigNew()[cewePrometer.getPLogger()][channelIndex].getRawData();
            }
            return ChannelConfigurationParser.toChannelInfoNewFw(rawData);
        }
    }

    /**
     * Split the complete register consisting of multiple records into
     * individual/separate records, and objectify them into a list of
     * ProRegisters.  ProRegisters, because they are easily parseable.
     *
     * @param buffer
     * @return
     */
    private List<ProRegister> splitIntervals(String buffer) {
        List<ProRegister> list = new ArrayList<ProRegister>();

        boolean eof = false;
        int openIdx = buffer.indexOf('(', 0);
        int closeIdx = buffer.indexOf(')', 0);

        while (!eof && (openIdx != -1) && (closeIdx != -1)) {

            String interval = buffer.substring(openIdx, closeIdx + 1);
            eof = interval.indexOf("(EOF)") != -1;
            if (!eof) {
                ProRegister pr = new ProRegister(interval);
                pr.setCeweProMeter(cewePrometer); /* hmmm .... */
                list.add(pr);
            }

            openIdx = buffer.indexOf('(', closeIdx);
            closeIdx = buffer.indexOf(')', openIdx);

        }
        return list;
    }

    public String fetchProfileData(Date from) throws IOException {
        Date lastReading = new Date(from.getTime());
        writeLoggerOffset(lastReading);
        StringBuffer buffer = new StringBuffer();
        boolean eof = false;
        RetryHandler handler = new RetryHandler(getRetries());
        while (!eof) {
            try {
                while (!eof) {
                    String result = getRlogNextRecord().getRawData(false);
                    eof = result.indexOf("(EOF)") != -1;
                    buffer.append(result);
                    if (!eof) {
                        lastReading = getNextDateToRequestFromResponse(result);
                    }
                    handler.reset();
                }
            } catch (IOException e) {
                handler.logFailure(e);
                writeLoggerOffset(lastReading);
            }
        }
        return buffer.toString();
    }

    private void writeLoggerOffset(Date lastReading) throws IOException {
        cewePrometer.write(cewePrometer.toCmd(getRLogOffset(), getDateFormats().getQueryDateFormat().format(lastReading)));
    }

    private int getRetries() {
        return cewePrometer.getRetries();
    }

    private CeweDateFormats getDateFormats() {
        return cewePrometer.getDateFormats();
    }

    private ProRegister getRLogOffset() {
        return cewePrometer.getRegisters().getrLogOffset()[cewePrometer.getPLogger()];
    }

    private ProRegister getRlogNextRecord() {
        return cewePrometer.getRegisters().getrLogNextRecord()[cewePrometer.getPLogger()];
    }


    public ProfileData readProfileData(Date from) throws IOException {
        String rawProfileData = fetchProfileData(from);
        return toProfileData(rawProfileData);
    }
}
