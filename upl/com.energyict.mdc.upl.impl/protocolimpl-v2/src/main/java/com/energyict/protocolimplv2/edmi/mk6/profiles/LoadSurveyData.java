/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimpl.edmi.common.command.GeniusFileAccessReadCommand;
import com.energyict.protocolimpl.edmi.common.command.GeniusFileAccessSearchCommand;
import com.energyict.protocolimpl.edmi.common.core.AbstractRegisterType;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeParser;
import com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author koen
 */
public class LoadSurveyData implements Serializable {

    private static final int MAX_DATA_PACKET_SIZE = 2048;

    private LoadSurvey loadSurvey;
    private List<IntervalData> collectedIntervalData;
    private MK6ProfileIntervalStatusBits intervalStatusBits;

    private byte[] data;
    private Date firstTimeStamp;
    private int numberOfRecords;

    /**
     * Creates a new instance of LoadSurveyData
     */
    public LoadSurveyData(LoadSurvey loadSurvey) throws ProtocolException {
        this.setLoadSurvey(loadSurvey);
        this.setIntervalStatusBits(new MK6ProfileIntervalStatusBits());
    }

    public void readFile(Date from) throws ProtocolException {
        Pair<Integer, byte[]> pair;
        if (!getLoadSurvey().preventCrossingIntervalBoundaryWhenReading()) { // Don't care that we cross an interval boundary when reading data (all data read remains valid)
            pair = doReadFile(from);
        } else { // Prevent cross of interval boundary (to avoid we read out data skewed with one interval) - this only applies to certain older meters
            long startRecord;
            long updatedStartRecord;
            do {
                startRecord = getLoadSurvey().getCommandFactory()
                        .getGeniusFileAccessInfoCommand(getLoadSurveyDataRegisterId())
                        .getStartRecord();
                pair = doReadFile(from);
                updatedStartRecord = getLoadSurvey().getCommandFactory()
                        .getGeniusFileAccessInfoCommand(getLoadSurveyDataRegisterId())
                        .getStartRecord();
            } while (startRecord != updatedStartRecord);
        }

        setNumberOfRecords(pair.getFirst());
        setData(pair.getLast());
        buildCollectedIntervalData();
    }

    private Pair<Integer, byte[]> doReadFile(Date from) throws ProtocolException {
        int records = 0;
        ByteArrayOutputStream byteArrayOutputStream;
        GeniusFileAccessReadCommand farc;
        GeniusFileAccessSearchCommand fasc;

        byteArrayOutputStream = new ByteArrayOutputStream();
        fasc = getLoadSurvey().getCommandFactory().getGeniusFileAccessSearchForwardCommand(getLoadSurveyDataRegisterId(), from);
        long startRecord = fasc.getStartRecord();
        setFirstTimeStamp(new Date(getLoadSurvey().getStartTime().getTime() + (startRecord * (getLoadSurvey().getProfileInterval() * 1000))));
        int nrOfRecords2Request = MAX_DATA_PACKET_SIZE / getLoadSurvey().getEntryWidth();

        do {
            farc = getLoadSurvey().getCommandFactory().getGeniusFileAccessReadCommand(
                    getLoadSurveyDataRegisterId(),
                    startRecord, nrOfRecords2Request, 0,
                    getLoadSurvey().getEntryWidth());
            records += farc.getNumberOfRecords();
            startRecord = farc.getStartRecord() + farc.getNumberOfRecords();
            byteArrayOutputStream.write(farc.getData(), 0, farc.getData().length);
        } while ((getLoadSurvey().getStoredEntries() - (farc.getStartRecord() + farc.getNumberOfRecords())) > 0);
        return new Pair<>(records, byteArrayOutputStream.toByteArray());
    }

    private int getLoadSurveyDataRegisterId() {
        return (getLoadSurvey().getRegisterId() << 16) | MK6RegisterInformation.LOAD_SURVEY_FILE_ACCESS_POINT.getRegisterId();
    }

    public int getStatus(int intervalIndex) {
        int offset = intervalIndex * loadSurvey.getEntryWidth();
        return ProtocolTools.getIntFromBytes(getData(), offset, 2);
    }

    private byte[] getData(int intervalIndex, int channelIndex) {
        int offset = intervalIndex * loadSurvey.getEntryWidth() + loadSurvey.getLoadSurveyChannels()[channelIndex].getOffset();
        return ProtocolUtils.getSubArray2(getData(), offset, loadSurvey.getLoadSurveyChannels()[channelIndex].getWidth());
    }

    public AbstractRegisterType[] getChannelValues(int intervalIndex) throws ProtocolException {
        AbstractRegisterType[] channelValues = new AbstractRegisterType[loadSurvey.getNrOfChannels()];
        RegisterTypeParser rtp = new RegisterTypeParser(loadSurvey.getCommandFactory().getProtocol().getTimeZone());
        for (int channel = 0; channel < loadSurvey.getNrOfChannels(); channel++) {
            AbstractRegisterType channelValue = rtp.parse2Internal((char) loadSurvey.getLoadSurveyChannels()[channel].getType(), getData(intervalIndex, channel));
            channelValues[channel] = channelValue;
        }
        return channelValues;
    }

    private void buildCollectedIntervalData() throws ProtocolException {
        this.collectedIntervalData = new ArrayList<>();
        Calendar cal = ProtocolUtils.getCleanCalendar(getLoadSurvey().getCommandFactory().getProtocol().getTimeZone());
        cal.setTime(getFirstTimeStamp());
        for (int interval = 0; interval < getNumberOfRecords(); interval++) {
            IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()));
            for (int channel = 1; channel < getLoadSurvey().getNrOfChannels(); channel++) {
                int protocolStatus = getChannelValues(interval)[0].getBigDecimal().intValue();
                int eiStatus = getIntervalStatusBits().getEisStatusCode(protocolStatus);
                intervalData.setEiStatus(eiStatus);
                intervalData.setProtocolStatus(protocolStatus);
                intervalData.addValue(getChannelValues(interval)[channel].getBigDecimal());
            }
            this.collectedIntervalData.add(intervalData);
            cal.add(Calendar.SECOND, getLoadSurvey().getProfileInterval());
        }
    }

    public LoadSurvey getLoadSurvey() {
        return loadSurvey;
    }

    public void setLoadSurvey(LoadSurvey loadSurvey) {
        this.loadSurvey = loadSurvey;
    }

    public MK6ProfileIntervalStatusBits getIntervalStatusBits() {
        return intervalStatusBits;
    }

    private void setIntervalStatusBits(MK6ProfileIntervalStatusBits intervalStatusBits) {
        this.intervalStatusBits = intervalStatusBits;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Date getFirstTimeStamp() {
        return this.firstTimeStamp;
    }

    public void setFirstTimeStamp(Date firstTimeStamp) {
        this.firstTimeStamp = firstTimeStamp;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public List<IntervalData> getCollectedIntervalData() {
        return collectedIntervalData;
    }
}