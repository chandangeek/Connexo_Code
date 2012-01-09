package com.elster.protocolimpl.lis100.profile;

import com.elster.protocolimpl.lis100.ChannelData;
import com.elster.protocolimpl.lis100.DeviceData;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 21.01.11
 * Time: 13:55
 */
public class Lis100Profile {

    /* reference to device data */
    private DeviceData deviceData;
    /* reference to the logger */
    private Logger logger;

    private IntervalDataMap ivdm;
    private ArrayList events = new ArrayList();

    /**
     * constructor for class to get profile data
     *
     * @param deviceData - reference to data of device
     * @param logger - reference to logger
     */
    public Lis100Profile(DeviceData deviceData, Logger logger) {
        this.deviceData = deviceData;
        this.logger = logger;
        ivdm = new IntervalDataMap(deviceData.getNumberOfChannels());
    }

    /**
     * build channel infos of read channel data
     *
     * @return list of channel infos
     * @throws IOException - in case of an error
     */
    public List<ChannelInfo> buildChannelInfo() throws IOException {

        ArrayList<ChannelInfo> result = new ArrayList<ChannelInfo>();

        for (int i = 0; i < deviceData.getNumberOfChannels(); i++) {
            ChannelData cd = deviceData.getChannelData(i, null, null);
            result.add(cd.getAsChannelInfo());
        }

        return result;
    }

    /**
     * read the channel profile data
     *
     * @param from - from date
     * @param to   - to date
     *
     * @return list of interval data of all channels
     *
     * @throws IOException in case of errors
     */
    public List<IntervalData> getIntervalData(Date from, Date to) throws IOException {

        ivdm.clear();
        events.clear();

        /* read out all channels... */
        for (int i = 0; i < deviceData.getNumberOfChannels(); i++) {

            ChannelData cd = deviceData.getChannelData(i, from, new IVDataStreamReader(deviceData.getObjectFactory()));

            try {
                if (cd.getRawData() != null){
                    Lis100Processing pr = new Lis100Processing(cd);
                    logger.info("- processing data for channel " + cd.getChannelNo());
                    pr.processReadData(from, to);
                    logger.info("- processed data: " + pr.getProcessedData().size() + " values, " + pr.getEvents().size() + " events");
                    cd.clearRawData();
                    ivdm.addAll(pr.getProcessedData(), cd.getChannelNo());
                    events.addAll(pr.getEvents());
                }
            } catch (ProcessingException e) {
                if (logger != null) {
                    logger.severe(String.format("! channel %d: %s", cd.getChannelNo(), e.getMessage()));
                }
            }
        }

        //System.out.println(ivdm.toString());

        return ivdm.buildIntervalData(deviceData.getTimeZone());
    }

    public List getMeterEvents() {
        return events;
    }

    /**
     * as the name says
     *
     * @param mes          - list of meter events
     * @param intervalData - list of interval data
     */
    public void applyEvents(List<MeterEvent> mes, List<IntervalData> intervalData) {

        for (MeterEvent me : mes) {
            applyEvent(me, intervalData);
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


}
