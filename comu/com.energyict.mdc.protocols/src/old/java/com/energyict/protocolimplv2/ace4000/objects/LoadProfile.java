package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author gna
 */
public class LoadProfile extends AbstractActarisObject {

    private int intervalInSeconds;
    private ProfileData profileData;
    private Date from = null;
    private Date toDate = null;

    public LoadProfile(ObjectFactory of) {
        super(of);
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Request all loadProfileIntervals
     */
    protected String prepareXML() {
        Document doc = createDomDocument();

        Element root = doc.createElement(XMLTags.MPULL);
        doc.appendChild(root);
        Element md = doc.createElement(XMLTags.METERDATA);
        root.appendChild(md);
        Element s = doc.createElement(XMLTags.SERIALNUMBER);
        s.setTextContent(getObjectFactory().getAce4000().getSerialNumber());
        md.appendChild(s);
        Element t = doc.createElement(XMLTags.TRACKER);
        t.setTextContent(Integer.toString(getTrackingID(), 16));
        md.appendChild(t);

        if (getFrom() == null && toDate == null) {
            Element lp = doc.createElement(XMLTags.REQLPALL);
            md.appendChild(lp);
        } else {
            Element lp = doc.createElement(XMLTags.REQLP);
            lp.setTextContent(getHexDate(from) + getHexDate(toDate));
            md.appendChild(lp);
        }

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    protected void parse(Element mdElement)  {
        if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPR)) {
            parseLoadProfile(mdElement.getTextContent(), false, 0);
        } else if (mdElement.getNodeName().equalsIgnoreCase(XMLTags.LOADPRABS)) {
            String scale = mdElement.getAttribute(XMLTags.SCALE);
            parseAbsoluteLoadProfile(scale, mdElement.getTextContent());
        }
    }

    private void parseLoadProfile(String data, boolean absoluteValues, int scale)  {
        int offset = 0;
        byte[] decoded = new Base64EncoderDecoder().decode(data);

        Date timeStamp = getObjectFactory().convertMeterDateToSystemDate(getNumberFromB64(decoded, offset, 4));
        offset += 4;
        Calendar cal = Calendar.getInstance();
        cal.setTime(timeStamp);

        setIntervalInSeconds(60 * getNumberFromB64(decoded, offset, 1));
        offset += 1;

        int numberOfValues = getNumberFromB64(decoded, offset, 1);
        offset += 1;

        int flag = IntervalStateBits.OK;

        int storedValue = 0;
        if (!absoluteValues) {
            storedValue = getNumberFromB64(decoded, offset, 4);
            offset += 4;
        }

        List<IntervalData> intervalDatas = new ArrayList<>();
        List<IntervalValue> intervalValues;

        for (int i = 0; i < numberOfValues; i++) {
            int increase = getNumberFromB64(decoded, offset, absoluteValues ? 4 : 2);

            if (absoluteValues) {
                storedValue = increase;       //Absolute value was parsed, length is 4 bytes
            } else {
                storedValue += increase;      //Incremental value was parsed, length is 2 bytes
            }
            offset += absoluteValues ? 4 : 2;

            int alarmFlags = getNumberFromB64(decoded, offset, 2);
            offset += 2;

            int tariffRate = getNumberFromB64(decoded, offset, 1);
            offset += 1;

            intervalValues = new ArrayList<>(1);
            IntervalValue intervalValue = new IntervalValue(storedValue, alarmFlags, getEiStatus(alarmFlags) | flag);
            intervalValues.add(intervalValue);
            intervalDatas.add(new IntervalData(cal.getTime(), intervalValue.getEiStatus(), intervalValue.getProtocolStatus(), tariffRate, intervalValues));
            cal.add(Calendar.SECOND, getIntervalInSeconds());
        }

        getProfileData().setChannelInfos(getSingleChannelInfo(scale));
        getProfileData().getIntervalDatas().addAll(intervalDatas);
        getProfileData().sort();
    }

    private int getEiStatus(int alarmFlags) {
        int result = 0;
        if (isBitSet(alarmFlags, 0)) {
            result += IntervalStateBits.POWERDOWN;
        }
        if (isBitSet(alarmFlags, 1)) {
            result += IntervalStateBits.CORRUPTED;
        }
        if (isBitSet(alarmFlags, 2)) {
            result += IntervalStateBits.CORRUPTED;
        }
        if (isBitSet(alarmFlags, 3)) {
            result += IntervalStateBits.WATCHDOGRESET;
        }
        if (isBitSet(alarmFlags, 4)) {
            result += IntervalStateBits.WATCHDOGRESET;
        }
        if (isBitSet(alarmFlags, 5)) {
            result += IntervalStateBits.BADTIME;
        }
        if (isBitSet(alarmFlags, 6)) {
            result += IntervalStateBits.DEVICE_ERROR;
        }
        if (isBitSet(alarmFlags, 8)) {
            result += IntervalStateBits.PHASEFAILURE;
        }
        if (isBitSet(alarmFlags, 9)) {
            result += IntervalStateBits.OTHER;
        }
        if (isBitSet(alarmFlags, 11)) {
            result += IntervalStateBits.REVERSERUN;
        }
        if (isBitSet(alarmFlags, 12)) {
            result += IntervalStateBits.DEVICE_ERROR;
        }
        if (isBitSet(alarmFlags, 14)) {
            result += IntervalStateBits.OTHER;
        }
        if (isBitSet(alarmFlags, 15)) {
            result += IntervalStateBits.OTHER;
        }
        return result;
    }

    private void parseAbsoluteLoadProfile(String scaleStr, String data) {
        int scale = (scaleStr == null || scaleStr.isEmpty()) ? 0 : Integer.parseInt(scaleStr);

        byte[] decoded = new Base64EncoderDecoder().decode(data);

        int numberOfValues = decoded[5] & 0xFF;
        int length1 = 6 + numberOfValues * 6;
        int length2 = 6 + numberOfValues * 15;

        if (decoded.length == length1) {
            parseLoadProfile(data, true, scale);
        } else if (decoded.length == length2) {

            int offset = 0;

            Date timeStamp = getObjectFactory().convertMeterDateToSystemDate(getNumberFromB64(decoded, offset, 4));
            offset += 4;
            Calendar cal = Calendar.getInstance();
            cal.setTime(timeStamp);

            setIntervalInSeconds(60 * getNumberFromB64(decoded, offset, 1));
            offset += 1;

            int flag = IntervalStateBits.OK;

            numberOfValues = getNumberFromB64(decoded, offset, 1);
            offset += 1;

            List<IntervalData> intervalDatas = new ArrayList<>();
            List<IntervalValue> intervalValues;

            for (int i = 0; i < numberOfValues; i++) {
                int activeImport = getNumberFromB64(decoded, offset, 4);
                offset += 4;
                int reactiveImport = getNumberFromB64(decoded, offset, 4);
                offset += 4;
                int activeExport = getNumberFromB64(decoded, offset, 4);
                offset += 4;

                int alarmFlags = getNumberFromB64(decoded, offset, 2);
                offset += 2;

                int tariffRate = getNumberFromB64(decoded, offset, 1);
                offset += 1;

                intervalValues = new ArrayList<>(3);
                intervalValues.add(new IntervalValue(activeImport, alarmFlags, flag | getEiStatus(alarmFlags)));
                intervalValues.add(new IntervalValue(activeExport, alarmFlags, flag | getEiStatus(alarmFlags)));
                intervalValues.add(new IntervalValue(reactiveImport, alarmFlags, flag | getEiStatus(alarmFlags)));

                intervalDatas.add(new IntervalData(cal.getTime(), getEiStatus(alarmFlags), alarmFlags, tariffRate, intervalValues));
                cal.add(Calendar.SECOND, getIntervalInSeconds());
            }

            getProfileData().setChannelInfos(getExtendedChannelInfo(scale));
            getProfileData().getIntervalDatas().addAll(intervalDatas);
        } else {
            getObjectFactory().log(Level.WARNING, "Unrecognized LP message length, cannot parse contents");
        }
    }

    public int getIntervalInSeconds() {
        return intervalInSeconds;
    }

    private void setIntervalInSeconds(int intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
    }

    public ProfileData getProfileData() {
        if (profileData == null) {
            profileData = new ProfileData();
        }
        return profileData;
    }

    private List<ChannelInfo> getSingleChannelInfo(int scale) {
        ChannelInfo ci = new ChannelInfo(0, "1.0.1.8.0.255", Unit.get(BaseUnit.WATTHOUR, scale));
        ci.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE));
        List<ChannelInfo> result = new ArrayList<>(1);
        result.add(ci);
        return result;
    }

    private List<ChannelInfo> getExtendedChannelInfo(int scale) {
        ChannelInfo ci1 = new ChannelInfo(0, "1.0.1.8.0.255", Unit.get(BaseUnit.WATTHOUR, scale));
        ci1.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE));
        ChannelInfo ci2 = new ChannelInfo(1, "1.0.3.8.0.255", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scale));
        ci2.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE));
        ChannelInfo ci3 = new ChannelInfo(2, "1.0.2.8.0.255", Unit.get(BaseUnit.WATTHOUR, scale));
        ci3.setCumulativeWrapValue(new BigDecimal(Integer.MAX_VALUE));

        List<ChannelInfo> result = new ArrayList<>(3);
        result.add(ci1);
        result.add(ci2);
        result.add(ci3);
        return result;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}