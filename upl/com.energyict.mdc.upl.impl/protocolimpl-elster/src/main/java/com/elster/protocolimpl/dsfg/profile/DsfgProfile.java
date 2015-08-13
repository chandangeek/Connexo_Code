package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.ChannelDefinition;
import com.elster.protocolimpl.dsfg.DsfgUtils;
import com.elster.protocolimpl.dsfg.ProtocolLink;
import com.elster.protocolimpl.dsfg.objects.ClockObject;
import com.elster.protocolimpl.dsfg.objects.SimpleObject;
import com.elster.protocolimpl.dsfg.telegram.DataBlock;
import com.elster.protocolimpl.dsfg.telegram.DataElement;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author gh
 * @since 5/26/2010
 */
public class DsfgProfile {

    /**
     * The used {@link ProtocolLink}
     */
    private final ProtocolLink link;

    /**
     * instance letter of reg. instance to use
     */
    private String registrationInstance = "";
    /**
     * instance letter of archive to readout
     */

    private ArchiveRecordConfig archiveStructure;

    /** The {@link DL220IntervalRecordConfig} from the meter */
    // private ArchiveRecordConfig dirc;

    /**
     * no of archive lines in a data block when requesting archive data from
     * device
     */
    @SuppressWarnings("unused")
    private int profileRequestBlockSize;

    /**
     * The List containing all {@link IntervalData}s
     */
    private List<IntervalData> intervalList = new ArrayList<IntervalData>();

    /* interval of archive in sec! */
    private int interval = 3600;

    @SuppressWarnings("unused")
    private String capturedObjects = "";

    private final TimeZone timezone;

    /** List of records with change events (before/after change) */
    // private List<IntervalArchiveRecord> specialEventRecords = new
    // ArrayList<IntervalArchiveRecord>();

    /**
     * Default constructor
     *
     * @param link             - the use {@link ProtocolLink}
     * @param archiveStructure - structure of the archive
     */
    public DsfgProfile(ProtocolLink link, ArchiveRecordConfig archiveStructure) {
        this.link = link;
        this.registrationInstance = link.getRegistrationInstance();
        this.archiveStructure = archiveStructure;
        this.timezone = link.getTimeZone();
    }

    /**
     * @return the number of channels
     * @throws IOException
     */
    public int getNumberOfChannels() throws IOException {
        return archiveStructure.getNumberOfChannels();
    }

    /**
     * @return the interval of the Profile
     * @throws IOException when something happens during the read
     */
    public int getInterval() throws IOException {
        return this.interval;
    }

    /**
     * Setter for the interval
     *
     * @param interval - the interval to set
     */
    public void setInterval(int interval) {
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

        SimpleObject unitreader;

        for (int i = 0; i < getNumberOfChannels(); i++) {

            ChannelDefinition cd = archiveStructure.getChannelDefinition(i);

            unitreader = new SimpleObject(link, cd.getValueUnitAddress());
            String unit = unitreader.getValue();

            ChannelInfo ci = new ChannelInfo(i, "Channel " + i,
                    getValueUnit(unit));

            if (cd.getChannelType().equalsIgnoreCase("C")) {
                ci.setCumulative();
                /* We also use the deprecated method for 8.3 versions */
                int ov = 1;
                for (int j = 0; j < cd.getChannelOv(); j++) {
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
     * @return the {@link Unit} for the channel
     * @throws IOException when reading the unit failed
     */
    public Unit getValueUnit(String unit) throws IOException {
        return DsfgUtils.getUnitFromString(unit);
    }

    /**
     * Get interval data within the request period
     *
     * @param from - the initial date for the interval data
     * @param to   - the end date for the interval data
     * @return the requested interval data
     * @throws IOException when reading of the data failed
     */
    public List<IntervalData> getIntervalData(Date from, Date to)
            throws IOException {

        IntervalDataMap data = new IntervalDataMap();

        ReadoutInfo roi = new ReadoutInfo(from, to);

        for (int i = 0; i < getNumberOfChannels(); i++) {
            readChannelArchiveData(i, data, roi);
			
			/* be sure that there is data for the requested date range... */
			/* 06/30/2010 gh */
            if (roi.getOnoFrom() < 0) {
                break;
            }
        }

        List<IntervalData> result = data.buildIntervalData(link.getTimeZone());

        cleanSuspiciousTimeStamps(result, interval);

        return result;
    }

    /**
     * read out data of one archive channel and put data in map
     *
     * @param index - channel to readout
     * @param data  - map to add the data to
     * @param roi   - readout info
     * @throws IOException
     */
    private void readChannelArchiveData(int index, IntervalDataMap data,
                                        ReadoutInfo roi) throws IOException {

        DataBlock request;
        DataBlock answer;

        Date prevDate = new Date(0);

        ChannelDefinition cd = archiveStructure.getChannelDefinition(index);

        String boxAddress = cd.getValueProfileData();

        // check if begin and end archive line numbers already set
        if (!roi.isOnoRangeSet()) {
            // first readout is done with begin and end date
            // data request via date from ... to
            RememberedElements flTotal = new RememberedElements();
            RememberedElements flRead = new RememberedElements();
			
			/* from & to date are UTC -> recalc to local time! */
			/* 07/12/2010 gh */
            long from = ClockObject.calendarToRaw(roi.getFromDate(), link.getTimeZone());
            flRead.setLast(new DataElement(boxAddress, null, from, null, null));
            long to = ClockObject.calendarToRaw(roi.getUntilDate(), link.getTimeZone());
            flTotal.setLast(new DataElement(boxAddress, null, to, null, null));

            int c = 0;
            do {
				/* the start is the last read date + 1 */
                flRead.setFirst(flRead.getLast());

                c++;
                link.getLogger().info(String.format("Box %d - Date read %d: %s - %s", index, c, flRead.getFirst().getDate().toString(),
                        flTotal.getLast().getDate().toString()));

                request = new DataBlock(
                        registrationInstance,
                        'A',
                        'J',
                        'Z',
                        new DataElement[]{
                                new DataElement(
                                        boxAddress,
                                        null,
                                        flRead.getFirst().getDateLong() + 1,
                                        null, null),
                                new DataElement(boxAddress, null,
                                        flTotal.getLast().getDate(),
                                        null, null)});
                answer = link.getDsfgConnection().sendRequest(request);
                flRead = copyElementsToIntervalDataMap(data, answer, index, prevDate);

                if (!flTotal.isFirstSet()) {
                    flTotal.setFirst(flRead.getFirst());
                }
				
				/* as long as data is not complete, continue... */
            } while (answer.getTypeOfBlock() == 'U');
			
			/* be sure that there is data for the requested date range... */
			/* 06/30/2010 gh */
            if (flTotal.getFirst() != null) {
                roi.setOnoFrom(flTotal.getFirst().getOno());
                roi.setOnoUntil(flRead.getLast().getOno());
            }

        } else {
            // following readouts are done with archive line numbers
            int c = 0;
            long onoBegin = roi.getOnoFrom() - 1;
            while (onoBegin < roi.getOnoUntil()) {
                onoBegin++;
                long onoTop = onoBegin + 100;
                if (onoTop > roi.getOnoUntil()) {
                    onoTop = roi.getOnoUntil();
                }

                c++;
                link.getLogger().info(String.format("Box %d -  ONO read %d: %d - %d", index, c, onoBegin, onoTop));

                // do request data
                request = new DataBlock(registrationInstance, 'A', 'J', 'O',
                        new DataElement[]{
                                new DataElement(boxAddress, null, (Long) null,
                                        onoBegin, null),
                                new DataElement(boxAddress, null, (Long) null,
                                        onoTop, null)});
                answer = link.getDsfgConnection().sendRequest(request);
                copyElementsToIntervalDataMap(data, answer, index, prevDate);
                onoBegin = onoTop;
            }
        }
    }

    private RememberedElements copyElementsToIntervalDataMap(
            IntervalDataMap data, DataBlock answer, int index, Date prevDate) {
        RememberedElements result = new RememberedElements();

        DataElement de = null;

        for (int i = 0; i < answer.getNumberOfElements(); i++) {
            de = answer.getElementAt(i);
            if (!result.isFirstSet()) {
                result.setFirst(de);
            }
            Date utcDate = ClockObject.localDateToUTC(de.getDateLong(), timezone);
            Date corDate = ClockObject.checkDate(utcDate, prevDate, timezone);
            data.addElement(corDate, index, de);
            //Warning: this will cause trouble if date becomes immutable!
            prevDate.setTime(corDate.getTime());
        }
        result.setLast(de);
        return result;
    }


    /**
     * Getter for the {@link #intervalList}
     *
     * @return the {@link #intervalList}
     */
    @SuppressWarnings({"unused"})
    public List<IntervalData> getIntervalList() {
        return this.intervalList;
    }

    private void cleanSuspiciousTimeStamps(List<IntervalData> lid, final int intervalInSec) {
        IntervalData line;

        for (int i = 0; i < lid.size(); ) {
            line = lid.get(i);

            final long time = line.getEndTime().getTime() / 1000;
            final long timeDiv = time / intervalInSec;
            final long timeRounded = timeDiv * intervalInSec;

            if (time != timeRounded) {
                // timestamp is not on a full interval boundary...
                // so remove from list!
                lid.remove(i);
            } else {
                i++;
            }
        }
    }
}
