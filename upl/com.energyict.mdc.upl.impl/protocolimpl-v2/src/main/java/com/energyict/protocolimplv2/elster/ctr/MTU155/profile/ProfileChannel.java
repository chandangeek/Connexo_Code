package com.energyict.protocolimplv2.elster.ctr.MTU155.profile;

import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConfigurationException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRExceptionWithIntervalData;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRExceptionWithProfileData;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace_C;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.CTRObjectInfo;

import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannel {

    private final RequestFactory requestFactory;

    private TimeZone deviceTimeZone = null;
    private CTRObjectID channelObjectId = null;
    private PeriodTrace_C period = null;

    private final Calendar toCalendar;
    private final Calendar fromCalendar;
    private int channelId = 0;

    public ProfileChannel(RequestFactory requestFactory, CTRObjectID objectID, int profileInterval, Date startReadingTime, Date endReadingTime) {
        this.requestFactory = requestFactory;
        this.channelObjectId = objectID;
        this.fromCalendar = Calendar.getInstance(requestFactory.getTimeZone());
        fromCalendar.setTime(startReadingTime);

        Calendar endReadingCal = Calendar.getInstance(requestFactory.getTimeZone());
        if (endReadingTime != null) {
            endReadingCal.setTime(endReadingTime);
        }
        toCalendar = TraceCProfileParser.getStartOfGasDay(endReadingCal);
        this.period = new PeriodTrace_C(new TimeDuration(profileInterval));
    }

    /**
     * Get the period to use while requesting the profile data.
     * The period is extracted from the channel interval in EIServer
     *
     * @return
     */
    private PeriodTrace_C getPeriod() {
        return period;
    }

    /**
     * Getter for the request factory
     *
     * @return
     */
    private RequestFactory getRequestFactory() {
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
            deviceTimeZone = getRequestFactory().getTimeZone();
        }
        return deviceTimeZone;
    }

    private CTRObjectID getChannelObjectId() {
        return channelObjectId;
    }

    private int getIntervalInSeconds() {
        return period.getIntervalInSeconds();
    }

    /**
     * @return the profile data
     * @throws com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException
     */
    public ProfileData getProfileData() throws CTRExceptionWithProfileData {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        try {
            pd.setIntervalDatas(getIntervalData());
        } catch (CTRExceptionWithIntervalData e) {
            pd.setIntervalDatas(e.getIntervalDatas());
            pd = ProtocolTools.clipProfileData(fromCalendar.getTime(), toCalendar.getTime(), pd);
            throw new CTRExceptionWithProfileData(e.getException(), pd);
        }
        return ProtocolTools.clipProfileData(fromCalendar.getTime(), toCalendar.getTime(), pd);
    }

    /**
     * @return
     */
    private List<ChannelInfo> getChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        String symbol = CTRObjectInfo.getSymbol(getChannelObjectId().toString()) + " [" + getChannelObjectId().toString() + "]";
        Unit unit = CTRObjectInfo.getUnit(getChannelObjectId().toString());
        ChannelInfo info = new ChannelInfo(channelId++, symbol, unit);
        channelInfos.add(info);
        return channelInfos;
    }

    /**
     * @throws com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException
     */
    private List<IntervalData> getIntervalData() throws CTRExceptionWithIntervalData {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        Calendar ptrDate = (Calendar) fromCalendar.clone();
        int invalidCount = 0;
        try {
            while (ptrDate.before(toCalendar)) {
                ReferenceDate referenceDate = TraceCProfileParser.calcRefDate(ptrDate, getPeriod());
                Trace_CQueryResponseStructure traceCStructure = getRequestFactory().queryTrace_C(getChannelObjectId(), getPeriod(), referenceDate);
                if (isValidResponse(traceCStructure, referenceDate)) {
                    TraceCProfileParser parser = new TraceCProfileParser(traceCStructure, getDeviceTimeZone(), getProperties().removeDayProfileOffset());
                    intervalDatas.addAll(parser.getIntervalData(fromCalendar, toCalendar));
                    ptrDate = (Calendar) parser.getToCalendar().clone();
                } else {
                    if (invalidCount++ > maxAllowedInvalidResponses()) {
                        throw new CTRException("Received more than " + maxAllowedInvalidResponses() + " invalid responses.");
                    }
                }
            }
        } catch (CTRException e) {
            throw new CTRExceptionWithIntervalData(e, intervalDatas);
        }
        return intervalDatas;
    }

    private double maxAllowedInvalidResponses() {
        return getProperties().getMaxAllowedInvalidProfileResponses();
    }

    private boolean isValidResponse(Trace_CQueryResponseStructure traceCStructure, ReferenceDate referenceDate) throws CTRConfigurationException {
        CTRObjectID id = traceCStructure.getId();
        ReferenceDate receivedReferenceDate = traceCStructure.getDate();
        if ((id != null) && (id.is(getChannelObjectId().toString()))) {
            if ((receivedReferenceDate != null) && (Arrays.equals(referenceDate.getBytes(), receivedReferenceDate.getBytes()))) {
                PeriodTrace_C responsePeriod = traceCStructure.getPeriod();
                int meterInterval = responsePeriod.getIntervalInSeconds();
                int eiserverInterval = getIntervalInSeconds();
                if (meterInterval != eiserverInterval) {
                    throw new CTRConfigurationException("Channel interval is incorrect for channel [" + getChannelObjectId() + "]. " +
                            "Configuration of LoadProfile is [" + eiserverInterval + "s], " +
                            "but meter returned [" + meterInterval + "s]."
                    );
                }
                return true;
            } else {
                getLogger().warning("Received invalid response! Requested [" + referenceDate + "] but received [" + receivedReferenceDate + "]");
            }
        } else {
            getLogger().warning("Received invalid response! Requested [" + getChannelObjectId() + "] but received [" + id + "]");
        }
        return false;
    }
}
