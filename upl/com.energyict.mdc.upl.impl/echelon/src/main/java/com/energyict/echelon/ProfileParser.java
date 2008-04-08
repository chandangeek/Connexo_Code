package com.energyict.echelon;

import com.energyict.cbo.Unit;
import com.energyict.mdw.core.Channel;
import com.energyict.protocol.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /* TODO: refactoring: remove global variable 'sourceCode' as a parser should be stateless. */

    SourceCode sourceCode[];

    /**
     * Parse the echelon xml result into a load profile object.
     *
     * @param doc      Echelon xml result
     * @param timeZone device timezone
     * @param channels list of channels
     * @return Load ProfileData
     * @throws Exception
     */
    ProfileData toLoadProfile(Document doc, TimeZone timeZone, List<Channel> channels) throws Exception {

        ProfileData result = new ProfileData();

        int nrChannels = getNumberOfChannels(doc);
        sourceCode = new SourceCode[nrChannels];

        initChannelInfo(result, nrChannels, channels);

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

    /**
     * Parse the echelon xml result into a event profile object.
     *
     * @param doc Echelon xml result
     * @return Event ProfileData
     * @throws Exception
     */
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
     *
     * @param result     ProfileData
     * @param nrChannels nr of returned channels
     * @param channels   list of channels
     * @param nrChannels number of channels
     */
    private void initChannelInfo(ProfileData result, int nrChannels, List<Channel> channels) {
        ArrayList<ChannelInfo> ci = new ArrayList<ChannelInfo>();
        ChannelInfo channelInfo;
        for (int i = 0; i < nrChannels; i++) {
            channelInfo = new ChannelInfo(i, "channel" + i, Unit.getUndefined());
            if (channels.get(i).getCumulative()) {
                channelInfo.setCumulativeWrapValue(new BigDecimal(999999999));     // TODO: replace hard coded wrap value by configurable value
            }
            ci.add(channelInfo);
        }
        result.setChannelInfos(ci);
    }

    /**
     * create final channelInfo object, with description and actual unit's
     *
     * @param result ProfileData to paste into the channel info.
     */
    private void getChannelInfo(ProfileData result) {
        ArrayList<ChannelInfo> ci = new ArrayList<ChannelInfo>();
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
     *
     * @param intervalNode to be parsed into IntervalData
     * @param timeZone     of the RTU
     * @return IntervalData (with mapped status flags), null if date of interval in the future (or parse exception).
     */
    private IntervalData toInterval(Node intervalNode, TimeZone timeZone) {

        IntervalData intervalData;

        try {
            Date date = Util.getNodeDate((Element) intervalNode, Util.DATETIME_TAG, timeZone);

            Date now = new Date();
            if (now.before(date)) {
                return null;
            }

            intervalData = new IntervalData(date);

            int channelStatus;
            int eiStatus = mapIntervalStatus(Util.getNodeInt((Element) intervalNode, "EXTENDEDSTATUS"));

            int channelIndex = 0;
            for (Object o : Util.collectNodes(intervalNode, Util.CHANNEL_TAG, Util.ID_TAG)) {

                Node channelNode = (Node) o;

                channelStatus = mapChannelStatus(Util.getNodeInt((Element) channelNode, "EXTENDEDSTATUS"));

                int id = Util.getNodeInt((Element) channelNode, Util.ID_TAG);

                if (sourceCode[channelIndex] == null) {
                    sourceCode[channelIndex] = SourceCode.get(id);
                }

                channelIndex = channelIndex + 1;

                BigDecimal value = new BigDecimal(Util.getNodeValue((Element) channelNode, "VALUE"));
                if (value.equals(BigDecimal.ZERO)) {
                    channelStatus |= IntervalStateBits.MISSING;
                }

                eiStatus |= channelStatus;
                intervalData.addValue(value, 0, channelStatus);

            }

            intervalData.setEiStatus(eiStatus);
        } catch (ParseException ex) {
            return null;
        }

        return intervalData;

    }

    /**
     * retrieve number of channels parameter from the document
     *
     * @param aDocument is the xml document retrieved from NES
     * @return the number of channels
     */
    private int getNumberOfChannels(Document aDocument) {
        NodeList nodeList = aDocument.getElementsByTagName(Util.NUMBEROFCHANNELS_TAG);
        Node node = nodeList.item(0);
        String valueString = node.getFirstChild().getNodeValue();
        return Integer.parseInt(valueString);
    }

    private MeterEvent toMeterEvent(Element eventElement) throws ParseException {

        Date date = Util.getNodeDate(eventElement, Util.DATETIME_TAG);
        int eventNumber = Util.getNodeInt(eventElement, Util.EVENT_NUMBER_TAG);
        String eventData = Util.getNodeValue(eventElement, Util.EVENT_DATA_TAG);

        return EventMapper.getInstance().Map(date, eventNumber, eventData);
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
