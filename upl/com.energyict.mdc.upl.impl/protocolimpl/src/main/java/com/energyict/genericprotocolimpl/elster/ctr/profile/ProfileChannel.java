package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace_C;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;
import java.util.logging.Logger;

import static com.energyict.genericprotocolimpl.elster.ctr.profile.TraceCProfileParser.calcRefDate;
import static com.energyict.genericprotocolimpl.elster.ctr.profile.TraceCProfileParser.getStartOfGasDay;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannel {

    private final GprsRequestFactory requestFactory;
    private final Channel meterChannel;

    private TimeZone deviceTimeZone = null;
    private Rtu rtu = null;
    private String channelObjectId = null;
    private PeriodTrace_C period = null;

    private final Calendar toCalendar;
    private final Calendar fromCalendar;

    public ProfileChannel(GprsRequestFactory requestFactory, Channel meterChannel) {
        this(requestFactory, meterChannel, null);
    }

    public ProfileChannel(GprsRequestFactory requestFactory, Channel meterChannel, Calendar to) {
        this.requestFactory = requestFactory;
        this.meterChannel = meterChannel;
        this.toCalendar = getStartOfGasDay(to == null ? Calendar.getInstance(getDeviceTimeZone()) : (Calendar) to.clone());
        this.fromCalendar = getFromCalendar();
    }

    /**
     * Get the period to use while requesting the profile data.
     * The period is extracted from the channel interval in EIServer
     *
     * @return
     */
    private PeriodTrace_C getPeriod() {
        if (period == null) {
            period = new PeriodTrace_C(getMeterChannel().getInterval());
        }
        return period;
    }

    /**
     * Checks if the hours / minutes have an overflow. This indicates that a time shift is in progress.
     *
     * @param date: the date that needs to be checked
     * @return the real date without the overflow
     */
    private Date fixDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);

        if (hour > 23) {
            hour -= 30;
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minutes > 59) {
            minutes -= 60;
            cal.set(Calendar.MINUTE, minutes);
        }

        return cal.getTime();
    }


    /**
     * Getter for the request factory
     *
     * @return
     */
    private GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * Getter for the protocol properties
     *
     * @return
     */
    private MTU155Properties getProperties() {
        return getRequestFactory().getProperties();
    }

    /**
     * Getter for the protocol logger. The logger from the requestFactory is used.
     *
     * @return
     */
    private Logger getLogger() {
        return getRequestFactory().getLogger();
    }

    /**
     * Get the cached devcie time zone
     *
     * @return
     */
    private TimeZone getDeviceTimeZone() {
        if (deviceTimeZone == null) {
            deviceTimeZone = getRtu().getDeviceTimeZone();
        }
        return deviceTimeZone;
    }

    /**
     * Get the cached rtu
     *
     * @return
     */
    private Rtu getRtu() {
        if (rtu == null) {
            rtu = getMeterChannel().getRtu();
        }
        return rtu;
    }

    /**
     * @return
     */
    public Channel getMeterChannel() {
        return meterChannel;
    }

    /**
     * @return
     */
    private String getChannelObjectIdAsString() {
        if (channelObjectId == null) {
            channelObjectId = getProperties().getChannelConfig().getChannelObjectId(getChannelIndex() - 1);
        }
        return channelObjectId;
    }

    private CTRObjectID getChannelObjectId() {
        return new CTRObjectID(getChannelObjectIdAsString());
    }

    /**
     * @return
     */
    private int getChannelIndex() {
        return getMeterChannel().getLoadProfileIndex();
    }

    /**
     * @return the profile data
     * @throws CTRException
     */
    public ProfileData getProfileData() throws CTRException {
        if (getChannelObjectIdAsString() == null) {
            getLogger().warning("No channel config found for channel with loadProfileIndex [" + getChannelIndex() + "]");
            return new ProfileData();
        }

        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        pd.setIntervalDatas(getIntervalData());
        return ProtocolTools.clipProfileData(getFromCalendar().getTime(), toCalendar.getTime(), pd);
    }

    /**
     * @return
     */
    private List<ChannelInfo> getChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        String symbol = CTRObjectInfo.getSymbol(getChannelObjectIdAsString()) + " [" + getChannelObjectIdAsString() + "]";
        Unit unit = CTRObjectInfo.getUnit(getChannelObjectIdAsString());
        ChannelInfo info = new ChannelInfo(0, getChannelIndex() - 1, symbol, unit);
        channelInfos.add(info);
        return channelInfos;
    }

    /**
     * @throws CTRException
     */
    private List<IntervalData> getIntervalData() throws CTRException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        Calendar ptrDate = (Calendar) fromCalendar.clone();
        while (ptrDate.before(toCalendar)) {
            Trace_CQueryResponseStructure response = getRequestFactory().queryTrace_C(getChannelObjectId(), getPeriod(), calcRefDate(ptrDate, getPeriod()));
            TraceCProfileParser parser = new TraceCProfileParser(response, getDeviceTimeZone());
            intervalDatas.addAll(parser.getIntervalData(fromCalendar, toCalendar));
            ptrDate = (Calendar) parser.getToCalendar().clone();
        }
        return intervalDatas;
    }

    /**
     * @return
     */
    private Calendar getFromCalendar() {
        Date lastReading = getMeterChannel().getLastReading();
        if (lastReading == null) {
            lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastDayDate(getDeviceTimeZone());
        }
        Calendar cal = ProtocolUtils.getCleanCalendar(getDeviceTimeZone());
        cal.setTime(lastReading);
        return cal;
    }

}
