package com.energyict.echelon;

import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
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

    /* map EXTENDEDSTATUS of entire Interval to IntervalStateBit */
    private final static int[] CHN_STATUS = {
            -1,                         /* 0 (no status flag) */
            IntervalStateBits.OVERFLOW, /* 1 (overflow) */
            IntervalStateBits.SHORTLONG,/* 2 (partial interval due to common state) */
            IntervalStateBits.SHORTLONG,/* 3 (long interval due to common state) */
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

        int iStatus = Util.getNodeInt((Element) intervalNode, "EXTENDEDSTATUS");
        if (iStatus < INT_STATUS.length && INT_STATUS[iStatus] != -1)
            intervalData.addEiStatus(INT_STATUS[iStatus]);

        int channelIndex = 0;
        Iterator it = Util.collectNodes(intervalNode, Util.CHANNEL_TAG, Util.ID_TAG).iterator();
        while (it.hasNext()) {

            Node channelNode = (Node) it.next();

            int cStatus = Util.getNodeInt((Element) channelNode, "EXTENDEDSTATUS");
            if (cStatus < CHN_STATUS.length && CHN_STATUS[cStatus] != -1)
                intervalData.addEiStatus(CHN_STATUS[cStatus]);

            int id = Util.getNodeInt((Element) channelNode, Util.ID_TAG);

            if (sourceCode[channelIndex] == null)
                sourceCode[channelIndex] = SourceCode.get(id);
            channelIndex = channelIndex + 1;

            BigDecimal value = new BigDecimal(Util.getNodeValue((Element) channelNode, "VALUE"));
            if (value == null || value.equals(0)) {
                intervalData.setEiStatus(IntervalStateBits.MISSING);
            }

            intervalData.addValue(value);

        }

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

    public static void main(String[] args) throws Exception {

        ProfileParser parser = new ProfileParser();

        File file = new File("c:\\ep2.xml");
        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[(int) file.length()];
        fis.read(buffer);

        System.out.println(parser.toLoadProfile(Util.toDom(new String(buffer)), null));

    }


}
