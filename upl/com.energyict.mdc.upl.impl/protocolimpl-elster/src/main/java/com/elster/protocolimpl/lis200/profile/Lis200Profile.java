/**
 *
 */
package com.elster.protocolimpl.lis200.profile;

import com.elster.protocolimpl.lis200.LIS200Utils;
import com.elster.protocolimpl.lis200.objects.GenericArchiveObject;
import com.elster.protocolimpl.lis200.objects.IntervalObject;
import com.elster.protocolimpl.lis200.utils.RawArchiveLine;
import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;
import com.elster.utils.lis200.events.EventInterpreter;
import com.elster.utils.lis200.profile.IArchiveLineData;
import com.elster.utils.lis200.profile.IArchiveRawData;
import com.elster.utils.lis200.profile.ProcessArchiveData;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.commands.ArchiveEmptyException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.elster.protocolimpl.lis200.utils.utils.splitLine;

/**
 * Implementation of the general functionality of a LoadProfile.<br>
 * It is possible to configure <b>TWO</b> channels in the loadProfile. The meter
 * can have a High tariff and a low tariff. It is also possible to put the total
 * and the adjustable value(which will most likely be the same value)
 *
 * @author gna
 * @since 4-mrt-2010
 */
public class Lis200Profile implements IArchiveRawData {

    /**
     * The used {@link ProtocolLink}
     */
    private final ProtocolLink link;
    /**
     * instance of archive to readout
     */
    private int archiveInstance = 0;
    /**
     * address of interval object of archive
     */
    private IntervalObject archiveIntervalObj;
    /**
     * no of archive lines in a data block when requesting archive data from
     * device
     */
    private int profileRequestBlockSize;

    /**
     * instance of log book to read out events
     */
    private int logbookInstance;

    private EventInterpreter eventInterpreter;
    /**
     * The used {@link GenericArchiveObject}
     */
    private GenericArchiveObject archiveObject;
    /**
     * content of one archive line
     */
    RawArchiveLineInfo archiveLineInfo;
    /**
     * Class to hold archive data in "lines"
     */
    ArrayList<String> archiveData = new ArrayList<String>();
    /*
     * class to process read data
     */
    ProcessArchiveData pad = null;

    List<MeterEvent> mel = null;

    /* internal counter of lines during processing */
    int linePointer = 0;
    /**
     * The used {@link Unit} (the unit is the same for each channel)
     */
    private String[] unit = null;

    private int interval = -1;

    /**
     * Default constructor
     *
     * @param link                    - the use {@link ProtocolLink}
     * @param archiveInstance         - the instance number of the archive
     * @param archiveStructure        - the values in an archive line
     * @param archiveIntervalObj      - the object where the interval of the archive is retrievable
     * @param logbookInstance         - the instance number of the logbook archive
     * @param profileRequestBlockSize - the size of the profileRequestBlocks
     * @param eventInterpreter        - the class to interpret the archive events
     */
    public Lis200Profile(ProtocolLink link, int archiveInstance,
                         String archiveStructure,
                         IntervalObject archiveIntervalObj, int logbookInstance,
                         int profileRequestBlockSize, EventInterpreter eventInterpreter) {
        this.link = link;
        this.archiveInstance = archiveInstance;
        this.archiveIntervalObj = archiveIntervalObj;
        this.logbookInstance = logbookInstance;
        this.profileRequestBlockSize = profileRequestBlockSize;
        this.eventInterpreter = eventInterpreter;

        pad = new ProcessArchiveData(this, link.getTimeZone());

        archiveLineInfo = new RawArchiveLineInfo(archiveStructure);
    }


    /**
     * @return the interval of the Profile
     * @throws IOException when something happens during the read
     */
    public int getInterval() throws IOException {
        if (this.interval == -1) {
            this.interval = archiveIntervalObj.getIntervalSeconds();
        }
        return this.interval;
    }

    /**
     * Setter for the interval
     *
     * @param interval - the interval to set
     */
    @SuppressWarnings("unused")
    protected void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Construct the channelInfos
     *
     * @return a list of {@link ChannelInfo}s
     * @throws IOException if an error occurred during the read of the
     *                     {@link ChannelInfo}s
     */
    @SuppressWarnings("deprecation")
    public List<ChannelInfo> buildChannelInfos() throws IOException {

        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        for (int i = 0; i < getNumberOfChannels(); i++) {

            int col = archiveLineInfo.getValueColumn(i);

            ChannelInfo ci = new ChannelInfo(i, "Channel " + i, getValueUnit(col));

            if (archiveLineInfo.isValueCounter(i)) {
                ci.setCumulative();
                /* We also use the deprecated method for 8.3 versions */
                int ov = 1;
                for (int j = archiveLineInfo.getValueOverFlow(i); j > 0; j--) {
                    ov *= 10;
                }
                ci.setCumulativeWrapValue(new BigDecimal(ov));
            }
            channelInfos.add(ci);
        }
        return channelInfos;
    }

    /**
     * Get the Unit list from the device and return the {@link Unit}
     *
     * @param index - index of value in the list
     * @return the {@link Unit} for the channel
     * @throws IOException when reading the unit failed
     */
    public Unit getValueUnit(int index) throws IOException {
        if (this.unit == null) {
            unit = splitLine(getArchive().getUnits());
        }
        return LIS200Utils.getUnitFromString(unit[index]);
    }

    // *******************************************************************************************
    //
    // reading & processing interval data
    //
    // *******************************************************************************************/

    /**
     * Get interval data within the request period
     * return empty list in case of archive is empty
     *
     * @param from - the initial date for the interval data
     * @param to   - the end date for the interval data
     * @return the requested interval data
     * @throws IOException when reading of the data failed
     */
    public List<IntervalData> getIntervalData(Date from, Date to)
            throws IOException {
        
        try {
            return buildIntervalData(getArchive().getIntervals(from, to,
                profileRequestBlockSize));
        } catch (ArchiveEmptyException archiveEmptyException) {
            return new ArrayList<IntervalData>();
        }
        
    }

    /**
     * Build the list of IntervalData
     * Attention:
     * The internal list of meter events will be filled with the events saved in archive data,
     * interval data will not be merged with that info
     *
     * @param rawData - the raw data returned from the device
     * @return a list of {@link IntervalData}
     * @throws IOException if an exception occurred during on of the read requests
     */
    protected List<IntervalData> buildIntervalData(String rawData)
            throws IOException {

        // split raw data to lines
        archiveData = splitRawDataInLines(rawData, archiveLineInfo.getNumberOfValuesPerLine());

        // process lines
        List<IntervalData> lid = pad.processArchiveData(true);

        // after processing, archiveData can be cleared
        archiveData.clear();

        removeDoublesFromIntervalData(lid);

        cleanSuspiciousTimeStamps(lid);

        return lid;
    }

    /**
     * private procedure to split the raw data read into "archive" lines
     *
     * @param rawData - archive data read
     * @param noOfValues - in an archive line
     * @return archive data as list of lines
     */
    private ArrayList<String> splitRawDataInLines(String rawData, int noOfValues) {

        ArrayList<String> result = new ArrayList<String>();

        int top = rawData.length() - noOfValues * 2;
        int end;
        int offset = 0;
        do {
            /* extract one record from raw data */
            end = LIS200Utils.getNextRecord(rawData, offset, noOfValues);
            result.add(rawData.substring(offset, end));
            offset = end;
        } while (offset < top);

        return result;
    }

    /**
     * removing "double" values of the list
     * <p/>
     * Used algorithm: start from the end and
     * remove all previous records with a date greater than the current
     *
     * @param lid - the list of IntervalData to "clean"
     */
    private void removeDoublesFromIntervalData(List<IntervalData> lid) {

        Date lastDate = null;
        IntervalData line;

        int i = lid.size();
        while (i > 0) {
            i--;
            line = lid.get(i);
            if (lastDate == null) {
                lastDate = line.getEndTime();
            } else {
                if (line.getEndTime().compareTo(lastDate) >= 0) {
                    lid.remove(i);
                } else {
                    lastDate = line.getEndTime();
                }
            }
        }
    }

    private void cleanSuspiciousTimeStamps(List<IntervalData> lid) {
        IntervalData line;

        try {
            final long intervalMs = getInterval() * 1000;

            for (int i = 0; i < lid.size(); ) {
                line = lid.get(i);

                final long time = line.getEndTime().getTime();
                final long timeDiv = time / intervalMs;
                final long timeRounded = timeDiv * intervalMs;

                if (time != timeRounded) {
                    // timestamp is not on a full interval boundary...
                    // so remove from list!
                    lid.remove(i);
                } else {
                    i++;
                }
            }
        } catch (IOException ignore) {
        }
    }

    // *******************************************************************************************
    //
    // reading & processing events
    //
    // *******************************************************************************************/

    /**
     * Get a list of meterEvents starting from the given fromDate
     *
     * @param from - the date to start reading from
     * @return a list of MeterEvents
     * @throws IOException if somethings happens dureing readout
     */
    public List<MeterEvent> getMeterEvents(Date from) throws IOException {

        GenericArchiveObject gaoEvents = new GenericArchiveObject(link,
                logbookInstance);
        try {
            String rawEvents = gaoEvents
                    .getIntervals(from, profileRequestBlockSize);
            return buildEventData(rawEvents);
        } catch (ArchiveEmptyException aee) {
            return new ArrayList<MeterEvent>();
        }
    }

    protected List<MeterEvent> buildEventData(String rawData) {

        RawArchiveLine ral;
        MeterEvent meterEvent;

        RawArchiveLineInfo logEntry = new RawArchiveLineInfo(",,TST,EVNT,CHKSUM");

        // split raw data to lines
        ArrayList<String> eventData = splitRawDataInLines(rawData, logEntry.getNumberOfValuesPerLine());

        // process lines
        mel = pad.getMeterEvents(eventInterpreter);

        for(String me: eventData) {
            try {
                ral = new RawArchiveLine(logEntry, me);
                meterEvent = eventInterpreter.interpretEvent(ral.getTimeStampUtc(link.getTimeZone()), ral.getEvent());
                mel.add(meterEvent);
            }
            catch (ParseException ignored) {

            }
        }

        return mel;
    }

    /**
     * as the name says
     *
     * @param mes          - list of meter events
     * @param intervalData - list of interval data
     */
    @SuppressWarnings({"unchecked"})
    public void applyEvents(List mes, List intervalData) {

        for (MeterEvent me : (List<MeterEvent>)mes) {
            applyEvent(me, (List<IntervalData>)intervalData);
        }
    }

    /**
     * Updates the interval status based on the information of a single event.
     *
     * @param event - the event to convert to intervalStatus
     * @param list  - the list of IntervalData
     */
    private void applyEvent(MeterEvent event, List<IntervalData> list) {
        if (list.size() == 0) {
            return;
        }
        if (event.getEiCode() == MeterEvent.OTHER) {
            return;
        }

        long interval = 60;
        IntervalData id1;
        int i = 0;
        IntervalData id2 = list.get(0);
        do {
            id1 = id2;
            i++;
            if (i < list.size()) {
                id2 = list.get(i);
                interval = (id2.getEndTime().getTime() - id1.getEndTime().getTime()) / 60000L;
            }
            id1.apply(event, (int) interval);
        } while (i < list.size());
    }

    /**
     * Getter for the {@link GenericArchiveObject}
     *
     * @return the genericArchiveObject
     */
    protected GenericArchiveObject getArchive() {
        if (this.archiveObject == null) {
            this.archiveObject = new GenericArchiveObject(link,
                    this.archiveInstance);
        }
        return this.archiveObject;
    }

    // *******************************************************************************************
    //
    // i n t e r f a c e  IArchiveRawData
    //
    // *******************************************************************************************/

    public void resetRecordPointer() {
        linePointer = 0;
    }

    public IArchiveLineData getNextRecord() {
        if (linePointer >= archiveData.size()) {
            return null;
        }
        try {
            return new RawArchiveLine(archiveLineInfo, archiveData.get(linePointer++));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the Number of channels
     */
    public int getNumberOfChannels() {
        return archiveLineInfo.getNumberOfChannels();
    }

    public boolean hasSystemStatus() {
        return archiveLineInfo.getSystemStateCol() >= 0;
    }

    public boolean hasInstanceStatus() {
        return archiveLineInfo.getNumberOfInstanceStateCols() > 0;
    }

    public boolean hasValueStatus() {
        return false;
    }

    public boolean hasEvent() {
        return archiveLineInfo.getEventCol() >= 0;
    }

}
