package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannelForSms {

    private final Channel meterChannel;
    private MTU155Properties properties;
    private final Date meterClock;
    private Logger logger;
    private Trace_CQueryResponseStructure response;

    public ProfileChannelForSms(Logger logger, MTU155Properties properties, Channel meterChannel, Trace_CQueryResponseStructure response, Date meterClock) {
        this.properties = properties;
        this.meterChannel = meterChannel;
        this.logger = logger;
        this.response = response;
        this.meterClock = meterClock;
    }

    /**
     * @return properties
     */
    private MTU155Properties getProperties() {
        return properties;
    }

    /**
     * @return logger
     */
    private Logger getLogger() {
        return logger;
    }

    /**
     * @return
     */
    private TimeZone getDeviceTimeZone() {
        return getRtu().getDeviceTimeZone();
    }

    /**
     * @return
     */
    private Rtu getRtu() {
        return getMeterChannel().getRtu();
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
    private String getChannelObjectId() {
        return getProperties().getChannelConfig().getChannelObjectId(getChannelIndex() - 1);
    }

    /**
     * @return
     */
    private int getChannelIndex() {
        return getMeterChannel().getLoadProfileIndex();
    }

    public Date getMeterClock() {
        return meterClock;
    }

    /**
     * @return
     * @throws CTRException
     */
    public ProfileData getProfileData() throws CTRException {
        if (getChannelObjectId() == null) {
            getLogger().warning("No channel config found for channel with loadProfileIndex [" + getChannelIndex() + "]");
            return new ProfileData();
        }

        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        pd.setIntervalDatas(getIntervalData());
        System.out.println(ProtocolTools.getProfileInfo(pd));
        return pd;
    }

    /**
     * @return
     */
    private List<ChannelInfo> getChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        String symbol = CTRObjectInfo.getSymbol(getChannelObjectId()) + " [" + getChannelObjectId() + "]";
        Unit unit = CTRObjectInfo.getUnit(getChannelObjectId());
        ChannelInfo info = new ChannelInfo(0, getChannelIndex() - 1, symbol, unit);
        channelInfos.add(info);
        return channelInfos;
    }

    /**
     * @throws CTRException
     */
    private List<IntervalData> getIntervalData() throws CTRException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        //Calendar fromCalendar = getFromCalendar();
        //Calendar toCalendar = Calendar.getInstance(getDeviceTimeZone());
        intervalDatas.addAll(getIntervalDatasFromResponse(response));
        //fromCalendar.setTime(getNewestIntervalDate(intervalDatas));
        return intervalDatas;
    }

    /**
     * @param intervalDatas
     * @return
     */
    private Date getNewestIntervalDate(List<IntervalData> intervalDatas) {
        Date newest = new Date(0);
        for (IntervalData intervalData : intervalDatas) {
            if (intervalData.getEndTime().after(newest)) {
                newest = intervalData.getEndTime();
            }
        }
        return newest;
    }

    /**
     * @param response
     * @return
     */
    private List<IntervalData> getIntervalDatasFromResponse(Trace_CQueryResponseStructure response) {
        List<IntervalData> intervals = new ArrayList<IntervalData>();
        Calendar startDate = response.getDate().getCalendar(getDeviceTimeZone());
        int startOfDay = response.getEndOfDayTime().getIntValue();
        startDate.add(Calendar.HOUR, startOfDay);
        int interval = getMeterChannel().getIntervalInSeconds();

        for (int i = 0; i < response.getTraceData().size(); i++) {
            if (i < response.getPeriod().getTraceCIntervalCount()) {
                AbstractCTRObject object = response.getTraceData().get(i);
                startDate.add(Calendar.SECOND, interval);
                Date endDate = new Date(startDate.getTimeInMillis());
                List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
                Qualifier qlf = object.getQlf();
                Object objectValue = object.getValue(0).getValue();
                if (objectValue instanceof Number) {
                    Number number = (Number) objectValue;
                    if (number instanceof BigDecimal) {
                        BigDecimal decimal = (BigDecimal) number;
                        decimal = decimal.movePointRight(qlf.getKmoltFactor());
                        intervalValues.add(new IntervalValue(decimal, 0, 0));
                    } else {
                        intervalValues.add(new IntervalValue(number, 0, 0));
                    }
                }
                if (endDate.before(getMeterClock())) {
                    intervals.add(new IntervalData(endDate, getIntervalStateBits(qlf), qlf.getQlf(), 0, intervalValues));
                } else {
                    break;
                }
            }
        }
        return intervals;
    }

    private int getIntervalStateBits(Qualifier qualifier) {
        if (qualifier.isInvalidMeasurement()) {
            return IntervalStateBits.CORRUPTED;
        } else if (qualifier.isSubjectToMaintenance()) {
            return IntervalStateBits.OTHER;
        } else if (qualifier.isReservedVal()) {
            return IntervalStateBits.OTHER;
        } else {
            return IntervalStateBits.OK;
        }
    }

    /**
     * @return
     */
    private Calendar getFromCalendar() {
        Date lastReading = getMeterChannel().getLastReading();
        if (lastReading == null) {
            lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getRtu());
        }
        Calendar cal = ProtocolUtils.getCleanCalendar(getDeviceTimeZone());
        cal.setTime(lastReading);
        return cal;
    }

}
