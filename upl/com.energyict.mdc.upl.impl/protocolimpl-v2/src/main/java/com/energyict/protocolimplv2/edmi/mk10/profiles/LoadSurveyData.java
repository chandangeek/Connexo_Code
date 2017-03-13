/*
 * LoadSurveyData.java
 *
 * Created on 3 april 2006, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimplv2.edmi.mk10.profiles;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.common.command.Atlas1FileAccessReadCommand;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.core.AbstractRegisterType;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeFloat;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeParser;
import com.energyict.util.Pair;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * @author koen
 */
public class LoadSurveyData {

    private static final int MAX_DATA_PACKET_SIZE = 2048;
    private static final int DEFAULT_MAX_ENTRIES = 0x10;

    private LoadSurvey loadSurvey;

    private byte[] data;
    private Date firstTimeStamp;
    private int numberOfRecords;
    private int maxNrOfEntries = -1;
    private List<IntervalData> collectedIntervalData;
    private MK10ProfileIntervalStatusBits intervalStatusBits;


    /**
     * Creates a new instance of LoadSurveyData
     */
    public LoadSurveyData(LoadSurvey loadSurvey) {
        this.setLoadSurvey(loadSurvey);
        this.setIntervalStatusBits(new MK10ProfileIntervalStatusBits());
    }

    public void readFile(Date from) throws ProtocolException {
        Pair<Integer, byte[]> pair;
        if (!getLoadSurvey().preventCrossingIntervalBoundaryWhenReading()) { // Don't care that we cross an interval boundary when reading data (all data read remains valid)
            long firstEntry = getLoadSurvey().getUpdatedFirstEntry();
            Date firstDate = new Date(getLoadSurvey().getStartTime().getTime() + (firstEntry * (getLoadSurvey().getProfileInterval() * 1000)));
            pair = doReadFile(from, firstEntry, firstDate);
        } else { // Prevent cross of interval boundary (to avoid we read out data skewed with one interval) - this only applies to certain older meters
            long firstEntry;
            Date firstDate;
            long updatedFirstEntry;
            do {
                firstEntry = getLoadSurvey().getUpdatedFirstEntry();
                firstDate = new Date(getLoadSurvey().getStartTime().getTime() + (firstEntry * (getLoadSurvey().getProfileInterval() * 1000)));
                pair = doReadFile(from, firstEntry, firstDate);
                updatedFirstEntry = getLoadSurvey().getUpdatedFirstEntry();
            } while (firstEntry != updatedFirstEntry);
        }

        setNumberOfRecords(pair.getFirst());
        setData(pair.getLast());
        buildCollectedIntervalData();
    }

    private Pair<Integer, byte[]> doReadFile(Date from, long firstEntry, Date firstDate) {
        int records = 0;
        long startRecord;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Atlas1FileAccessReadCommand farc;


        long seconds_div = (from.getTime() - firstDate.getTime()) / 1000;
        if (seconds_div < 0) {
            startRecord = firstEntry; // From date is earlier than first date - start reading from first date
        } else {
            startRecord = firstEntry + (seconds_div / getLoadSurvey().getProfileInterval()) + 1; // Move pointer forwards to start reading from the first entry after the given from date
        }

        farc = getCommandFactory().getAtlas1FileAccessReadCommand(getLoadSurvey().getLoadProfileDescription().getSurveyNr(), startRecord, 0x0001);
        startRecord = farc.getStartRecord(); // The actual start record (most likely the same as requested start record)
        setFirstTimeStamp(new Date(getLoadSurvey().getStartTime().getTime() + (startRecord * (getLoadSurvey().getProfileInterval() * 1000))));

        do {
            farc = getCommandFactory().getAtlas1FileAccessReadCommand(getLoadSurvey().getLoadProfileDescription().getSurveyNr(), startRecord, getMaximumEntries());
            startRecord += farc.getNumberOfRecords();
            records += farc.getNumberOfRecords();
            byteArrayOutputStream.write(farc.getData(), 0, farc.getData().length);
        } while ((getLoadSurvey().getLastEntry() - (farc.getStartRecord() + farc.getNumberOfRecords())) > 0);
        return new Pair<>(records, byteArrayOutputStream.toByteArray());
    }

    private int getMaximumEntries() {
        if (maxNrOfEntries == -1) {
            if (getLoadSurvey().getRecordSize() == 0) {
                this.maxNrOfEntries = DEFAULT_MAX_ENTRIES;
            } else {
                this.maxNrOfEntries = MAX_DATA_PACKET_SIZE / getLoadSurvey().getRecordSize();
                if (this.maxNrOfEntries < 0) {
                    this.maxNrOfEntries = DEFAULT_MAX_ENTRIES;
                }
            }
        }
        return maxNrOfEntries;
    }

    public int getStatus(int intervalIndex) throws IOException {
        int offset = intervalIndex * loadSurvey.getRecordSize();
        return ProtocolUtils.getInt(getData(), offset, 2);
    }

    private void buildCollectedIntervalData() throws ProtocolException {
        int positionOfInfoChannel = getLoadSurvey().getNrOfChannels() - 1;
        List<IntervalData> collectedIntervalData = new ArrayList<>();
        Calendar cal = ProtocolUtils.getCleanCalendar(getLoadSurvey().getTimeZone());
        cal.setTime(getFirstTimeStamp());

        for (int interval = 0; interval < getNumberOfRecords(); interval++) {
            IntervalData intervalData = new IntervalData(new Date(cal.getTime().getTime()));
            for (int channel = 0; channel < getLoadSurvey().getNrOfChannels() - 1; channel++) {
                int protocolStatus = getChannelValues(interval)[positionOfInfoChannel].getBigDecimal().intValue();
                int eiStatus = getIntervalStatusBits().getEisStatusCode(protocolStatus);
                intervalData.setEiStatus(eiStatus);
                intervalData.setProtocolStatus(protocolStatus);
                intervalData.addValue(getChannelValues(interval)[channel].getBigDecimal());
            }
            if (hasValidData(intervalData)) {
                collectedIntervalData.add(intervalData);
            }
            cal.add(Calendar.SECOND, getLoadSurvey().getProfileInterval());    // Successive records are end to and with no gaps (in case of power failure a all 0's record with status 'Missing' is present)
        }
        this.collectedIntervalData = collectedIntervalData;
    }

    /**
     * Test if the intervalData has valid data.<br/>
     * The data is considered invalid in case all intervalValues are marked with (and only) the missing flag.
     *
     * @param intervalData
     * @return true in case data is valid
     */
    private boolean hasValidData(IntervalData intervalData) {
        ListIterator it = intervalData.getIntervalValueIterator();
        while (it.hasNext()) {
            IntervalValue intervalValue = (IntervalValue) it.next();
            if (intervalValue.getEiStatus() != IntervalStateBits.MISSING) {
                return true;
            }
        }
        return false;
    }

    private AbstractRegisterType[] getChannelValues(int intervalIndex) throws ProtocolException {
        AbstractRegisterType[] channelValues = new AbstractRegisterType[loadSurvey.getNrOfChannels()];
        RegisterTypeParser rtp = new RegisterTypeParser(loadSurvey.getCommandFactory().getProtocol().getTimeZone());
        AbstractRegisterType channelValue;
        for (int channel = 0; channel < loadSurvey.getNrOfChannels(); channel++) {
            LoadSurveyChannel loadSurveyChannel = loadSurvey.getLoadSurveyChannels()[channel];
            int decimalPointPositionScaling = loadSurveyChannel.getDecimalPointPositionScaling();

            if (loadSurveyChannel.isStatusChannel()) {
                channelValue = rtp.parse2Internal('C', getData(intervalIndex, channel));
            } else {
                channelValue = rtp.parse2Internal(loadSurveyChannel.isInstantaneousChannel() ? 'I' : 'H', getData(intervalIndex, channel));
                if (!loadSurveyChannel.isInstantaneousChannel() && decimalPointPositionScaling != 1) {  //TODO: check behaviour in case decimal point position is set to 1
                    channelValue = new RegisterTypeFloat(channelValue.getBigDecimal().movePointLeft(decimalPointPositionScaling).floatValue());
                }
            }
            channelValues[channel] = channelValue;
        }
        return channelValues;
    }

    private byte[] getData(int intervalIndex, int channelIndex) {
        int offset = (intervalIndex * getLoadSurvey().getRecordSize()) + (channelIndex * 2);
        byte[] bytes = ProtocolUtils.getSubArray2(getData(), offset, getLoadSurvey().getLoadSurveyChannels()[channelIndex].getWidth());
        ArrayUtils.reverse(bytes); // Convert little endian to big endian
        return bytes;
    }

    public LoadSurvey getLoadSurvey() {
        return loadSurvey;
    }

    private void setLoadSurvey(LoadSurvey loadSurvey) {
        this.loadSurvey = loadSurvey;
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public Date getFirstTimeStamp() {
        return firstTimeStamp;
    }

    private void setFirstTimeStamp(Date firstTimeStamp) {
        this.firstTimeStamp = firstTimeStamp;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    private void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public MK10ProfileIntervalStatusBits getIntervalStatusBits() {
        return intervalStatusBits;
    }

    private void setIntervalStatusBits(MK10ProfileIntervalStatusBits intervalStatusBits) {
        this.intervalStatusBits = intervalStatusBits;
    }

    public List<IntervalData> getCollectedIntervalData() {
        return collectedIntervalData;
    }

    private CommandFactory getCommandFactory() {
        return getLoadSurvey().getCommandFactory();
    }
}