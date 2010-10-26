package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
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
public class ProfileChannel {

    private final GprsRequestFactory requestFactory;
    private final Channel meterChannel;

    public ProfileChannel(GprsRequestFactory requestFactory, Channel meterChannel) {
        this.requestFactory = requestFactory;
        this.meterChannel = meterChannel;
    }

    /**
     * @return
     */
    private GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * @return
     */
    private MTU155Properties getProperties() {
        return getRequestFactory().getProperties();
    }

    /**
     * @return
     */
    private Logger getLogger() {
        return getRequestFactory().getLogger();
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
        return pd;
    }

    /**
     * @return
     */
    private List<ChannelInfo> getChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        String symbol = CTRObjectInfo.getSymbol(getChannelObjectId());
        Unit unit = CTRObjectInfo.getUnit(getChannelObjectId());
        ChannelInfo info = new ChannelInfo(0, symbol, unit);
        channelInfos.add(info);
        return channelInfos;
    }

    /**
     * @throws CTRException
     */
    private List<IntervalData> getIntervalData() throws CTRException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        Calendar fromCalendar = getFromCalendar();
        Calendar toCalendar = Calendar.getInstance(getDeviceTimeZone());
        while (fromCalendar.before(toCalendar)) {
            intervalDatas.addAll(getIntervalDataBlock(getChannelObjectId(), fromCalendar));
            fromCalendar.setTime(getNewestIntervalDate(intervalDatas));
        }
        return intervalDatas;
    }

    /**
     * @param intervalDatas
     * @return
     */
    private Date getNewestIntervalDate(List<IntervalData> intervalDatas) {
        Date newest = null;
        for (IntervalData intervalData : intervalDatas) {
            if (intervalData.getEndTime().after(newest == null ? new Date(0) : newest)) {
                newest = intervalData.getEndTime();
            }
        }
        return newest;
    }

    /**
     * @param channelId
     * @param fromCalendar
     * @return
     * @throws CTRException
     */
    private List<IntervalData> getIntervalDataBlock(String channelId, Calendar fromCalendar) throws CTRException {
        CTRObjectID objectID = new CTRObjectID(channelId);
        PeriodTrace period = new PeriodTrace(2);
        ReferenceDate referenceDate = new ReferenceDate().parse(fromCalendar);
        Trace_CQueryResponseStructure response = getRequestFactory().queryTrace_C(objectID, period, referenceDate);
        return getIntervalDatasFromResponse(response);
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
        startDate.add(Calendar.SECOND, interval);
        for (int i = 0; i < response.getTraceData().size(); i++) {
            if (i < 24) {
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
                intervals.add(new IntervalData(endDate, getIntervalStateBits(qlf), qlf.getQlf(), 0, intervalValues));
            }
        }
        return intervals;
    }

    private int getIntervalStateBits(Qualifier qualifier) {
        if (qualifier.isInvalidMeasurement()) {
            return IntervalStateBits.CORRUPTED;
        } else if (qualifier.isSubjectToMaintenance()) {
            return IntervalStateBits.CORRUPTED;
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
        return ProtocolTools.createCalendar(2010, 10, 20, 0, 0, 0, 0, getDeviceTimeZone());
/*
        Date lastReading = getMeterChannel().getLastReading();
        if (lastReading == null) {
            lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getRtu());
        }
        Calendar cal = ProtocolUtils.getCleanCalendar(getDeviceTimeZone());
        cal.setTime(lastReading);
        return cal;
*/
    }

}
