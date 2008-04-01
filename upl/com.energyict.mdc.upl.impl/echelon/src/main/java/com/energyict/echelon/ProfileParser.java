package com.energyict.echelon;

import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * DOM-based parser for profile data.  Could be way more efficient with a
 * SAX parser.  But good enough for now.
 * <p/>
 * ChannelInfo data is only know after parsing profile data.  Therefore
 * first a dummy ChannelInfo is made, with only the correct number of
 * channels.
 * <p/>
 * After parsing the Profile, a new ChannelInfo is made with the correct
 * units.
 */

public class ProfileParser {

    SourceCode sourceCode[] = new SourceCode[8];

    /* map EXTENDEDSTATUS of entire Interval to IntervalStateBit */
    private final static int[] INT_STATUS = {
            -1,                          /* 0 (daylight savings time in effect) */
            IntervalStateBits.POWERDOWN, /* 1 (power fail within interval) */
            IntervalStateBits.SHORTLONG, /* 2 (clock set forward during interval) */
            IntervalStateBits.SHORTLONG, /* 3 (clock reset backward during interval) */
    };

    ProfileData toLoadProfile(Document doc, TimeZone timeZone) throws Exception {

        ProfileData result = new ProfileData();

        int nrChannels = getNumberOfChannels(doc);
        initChannelInfo(result, nrChannels);

        NodeList nl = doc.getElementsByTagName(Util.INTERVAL_TAG);
        IntervalData interval;
        for (int i = 0; i < nl.getLength(); i++) {
            interval = toInterval(nl.item(i), timeZone);
            if (interval != null) {
                result.addInterval(interval);
            }
        }
        getChannelInfo(result);

        return result;
    }

    ProfileData toEventProfile(Document doc) throws Exception {
        ProfileData result = new ProfileData();

        NodeList nl = doc.getElementsByTagName(Util.EVENT_TAG);
        for (int i = 0; i < nl.getLength(); i++) {
            result.addEvent(toMeterEvent((Element) nl.item(i)));
        }

        return result;
    }

    /**
     * create initial channelInfo object, with undefined unit
     */
    private void initChannelInfo(ProfileData result, int nrChannels) {
        ArrayList ci = new ArrayList();
        for (int i = 0; i < nrChannels; i++) {
            ci.add(new ChannelInfo(i, "channel" + i, Unit.getUndefined()));
        }
        result.setChannelInfos(ci);
    }

    /**
     * create final channelInfo object, with description and actual unit's
     */
    private void getChannelInfo(ProfileData result) {
        ArrayList ci = new ArrayList();
        for (int i = 0; i < sourceCode.length; i++) {
            if (sourceCode[i] != null) {
                SourceCode code = sourceCode[i];
                ci.add(new ChannelInfo(i, code.getDescription(), code.getUnit()));
            }
        }
        result.setChannelInfos(ci);
    }

    /**
     * convert an INTERVAL node to an IntervalData object
     */
    private IntervalData toInterval(Node intervalNode, TimeZone timeZone) throws ParseException {

        Date date = Util.getNodeDate((Element) intervalNode, Util.DATETIME_TAG, timeZone);

        Date now = new Date();
        if (now.before(date)) {
            return null;
        }

        IntervalData intervalData = new IntervalData(date);

        int channelStatus;
        int eiStatus = mapIntervalStatus(Util.getNodeInt((Element) intervalNode, "EXTENDEDSTATUS"));

        int channelIndex = 0;
        Iterator it = Util.collectNodes(intervalNode, Util.CHANNEL_TAG, Util.ID_TAG).iterator();
        while (it.hasNext()) {

            Node channelNode = (Node) it.next();

            channelStatus = mapChannelStatus(Util.getNodeInt((Element) channelNode, "EXTENDEDSTATUS"));

            int id = Util.getNodeInt((Element) channelNode, Util.ID_TAG);

            if (sourceCode[channelIndex] == null) {
                sourceCode[channelIndex] = SourceCode.get(id);
            }

            channelIndex = channelIndex + 1;

            BigDecimal value = new BigDecimal(Util.getNodeValue((Element) channelNode, "VALUE"));
            if (value == null || value.equals(0)) {
                channelStatus |= IntervalStateBits.MISSING;
            }

            eiStatus |= channelStatus;
            intervalData.addValue(value, 0, channelStatus);

        }

        intervalData.setEiStatus(eiStatus);

        return intervalData;

    }

    /**
     * retrieve number of channels parameter from the document
     */
    private int getNumberOfChannels(Document aDocument) {
        NodeList nodeList = aDocument.getElementsByTagName(Util.NUMBEROFCHANNELS_TAG);
        Node node = nodeList.item(0);
        String valueString = node.getFirstChild().getNodeValue();
        int result = Integer.parseInt(valueString);
        return result;
    }

    private MeterEvent toMeterEvent(Element eventElement) throws ParseException {

        Date date = Util.getNodeDate(eventElement, Util.DATETIME_TAG);
        int eventNumber = Util.getNodeInt(eventElement, Util.EVENT_NUMBER_TAG);
        String eventData = Util.getNodeValue(eventElement, Util.EVENT_DATA_TAG);

        MeterEvent event = EventMapper.getInstance().Map(date, eventNumber, eventData);
        return event;
    }

    private int mapChannelStatus(int status) {
        /*
                1 (overflow)
                2 (partial interval due to common state)
                3 (long interval due to common state)
                8 (MEP security failure)
                15 (M-Bus channel placeholder in effect)
         */

        switch (status) {
            case 1:
                return IntervalStateBits.OVERFLOW;
            case 2:
            case 3:
                return IntervalStateBits.SHORTLONG;
            case 8:
            case 15:
                return IntervalStateBits.OTHER;
            default:
                return IntervalStateBits.OK;
        }
    }

    private int mapIntervalStatus(int status) {
        /*
                0 (daylight savings time in effect)
                1 (power fail within interval)
                2 (clock set forward during interval)
                3 (clock reset backward during interval)
         */
        switch (status) {
            case 1:
                return IntervalStateBits.POWERDOWN;
            case 2:
            case 3:
                return IntervalStateBits.OTHER;
            default:
                return IntervalStateBits.OK;
        }
    }

}
