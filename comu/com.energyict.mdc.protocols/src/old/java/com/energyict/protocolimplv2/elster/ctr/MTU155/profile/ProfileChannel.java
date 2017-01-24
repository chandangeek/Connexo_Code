package com.energyict.protocolimplv2.elster.ctr.MTU155.profile;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConfigurationException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace_C;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.CTRObjectInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannel {

    private final RequestFactory requestFactory;

    private StartOfGasDayParser startOfGasDayParser;
    private TimeZone deviceTimeZone = null;
    private CTRObjectID channelObjectId = null;
    private PeriodTrace_C period = null;

    private final Calendar toCalendar;
    private final Calendar fromCalendar;
    private int channelId = 0;

    public ProfileChannel(RequestFactory requestFactory, StartOfGasDayParser startOfGasDayParser, CTRObjectID objectID, int profileInterval, Date startReadingTime, Date endReadingTime) {
        this.requestFactory = requestFactory;
        this.startOfGasDayParser = startOfGasDayParser;
        this.channelObjectId = objectID;
        this.fromCalendar = Calendar.getInstance(requestFactory.getTimeZone());
        fromCalendar.setTime(startReadingTime);

        Calendar endReadingCal = Calendar.getInstance(requestFactory.getTimeZone());
        if (endReadingTime != null) {
            endReadingCal.setTime(endReadingTime);
            toCalendar = getStartOfGasDayParser().getStartOfGasDay(endReadingCal);
        } else {
            toCalendar = getStartOfGasDayParser().getStartOfGasDay(Calendar.getInstance(requestFactory.getTimeZone()));
        }

        if (getRequestFactory().isEK155Protocol() &&
                (objectID.toString().equalsIgnoreCase("2.1.2") || objectID.toString().equalsIgnoreCase("2.0.2"))) {
            this.period = new PeriodTrace_C(PeriodTrace_C.HOURLY_FIRST_PART);

        } else {
            this.period = new PeriodTrace_C(new TimeDuration(profileInterval));
        }
    }

    /**
     * Get the period to use while requesting the profile data.
     *
     * Calculate the Period based on the channel interval in EIServer
     * Note: when the channel interval is hourly, period 1 (all 1h traces on the specified day) will be set.
     * For EK155 volume channels, this period is wrong (0x80 should be set (hourly traces from OFG+1 to OFG+12 on the specified day)).
     *
     * Procedure to readout hourly channels
     * 1. Send the first TraceC request to the device, with period 1.
     * 2. Parse the TraceC response:
     *     A. response contains 24 trace_data objects: period OK - continue with the next TraceC request
     *     B. response contains only 20 trace_data objects - period NOT OK - set period to 0x80 and resend the first TraceC request, now with period 0x80.
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
    public ProfileData getProfileData() throws CTRException {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        pd.setIntervalDatas(getIntervalData());
        return ProtocolTools.clipProfileData(fromCalendar.getTime(), toCalendar.getTime(), pd);
    }

    /**
     * @return
     */
    private List<ChannelInfo> getChannelInfos() {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        String symbol = CTRObjectInfo.getSymbol(getChannelObjectId().toString()) + " [" + getChannelObjectId().toString() + "]";
        Unit unit = CTRObjectInfo.getUnit(getChannelObjectId().toString());
        ChannelInfo info = new ChannelInfo(channelId++, symbol, unit);
        channelInfos.add(info);
        return channelInfos;
    }

    /**
     * @throws com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException
     */
    private List<IntervalData> getIntervalData() throws CTRException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        Calendar ptrDate = (Calendar) fromCalendar.clone();
        int invalidCount = 0;

        while (ptrDate.before(toCalendar)) {
            ReferenceDate referenceDate = getStartOfGasDayParser().calcRefDate(ptrDate, getPeriod());
            Trace_CQueryResponseStructure traceCStructure = getRequestFactory().queryTrace_C(getChannelObjectId(), getPeriod(), referenceDate);
            if (isValidResponse(traceCStructure, referenceDate)) {
                /** If we read daily profile data, we expect 24 objects - if we only get 20 trace_data objects instead, we know we are in wrong mode and should use splitted period (2 x 12 hours)!
                 * If in this case, do not further parse the response, but launch a new request, now with period 0x80.
                 *
                 * Procedure to readout hourly channels
                 * 1. Send the first TraceC request to the device, with period 1.
                 * 2. Parse the TraceC response:
                 *  A. response contains 24 trace_data objects: period OK - continue with the next TraceC request
                 *  B. response contains only 20 trace_data objects - period NOT OK, discard trace_CQueryResponse - set period to 0x80 and resend the first TraceC request, now with period 0x80.
                 **/

                if (!checkValidityOfPeriod(traceCStructure)) {
                    getPeriod().setPeriod(PeriodTrace_C.HOURLY_FIRST_PART);
                } else {
                    TraceCProfileParser parser = new TraceCProfileParser(traceCStructure, getDeviceTimeZone(), getStartOfGasDayParser(), getProperties().removeDayProfileOffset());
                    intervalDatas.addAll(parser.getIntervalData(fromCalendar, toCalendar));
                    ptrDate = (Calendar) parser.getToCalendar().clone();
                    // Check the period and if needed switch it
                    checkSwitchPeriod();
                }
            } else {
                if (invalidCount++ > maxAllowedInvalidResponses()) {
                    throw new CTRException("Received more than " + maxAllowedInvalidResponses() + " invalid responses.");
                }
            }
        }
        return intervalDatas;
    }

    /** Method to check if the used PeriodTrace_C is correct.
     *
     * If we read daily values, we expect 24 objects - if we only get 20 trace_data objects instead, we know we are in wrong mode and should use splitted period (2 x 12 hours)!
     *
     **/
    private boolean checkValidityOfPeriod(Trace_CQueryResponseStructure traceCStructure) {
        if (period.getPeriod()  == PeriodTrace_C.HOURLY) {
            if (traceCStructure.getTraceData().size() < period.getTraceCIntervalCount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check the period and if needed switch it.
     *
     * For EK155 device the hourly volume channels should be readout using splitted period.
     * E.g.: to retrieve all 24 hourly values for a specific day,
     * we must first request the first 12 values (OFG +1 to OFG +12), using period 0x80.
     * Then we must change period to 0x81, the response -for same reference day - will now contain values for OFG + 13 to OFG + 24
     *
     * Reading out of the next gas-day will require the same 2-step procedure, but with reference day now +1 day.
     */
    private void checkSwitchPeriod() {
        if (getPeriod().isHourlyFistPart()) {
            getPeriod().setPeriod(PeriodTrace_C.HOURLY_SECOND_PART);
        } else if (getPeriod().isHourlySecondPart()) {
            getPeriod().setPeriod(PeriodTrace_C.HOURLY_FIRST_PART);
        }
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

    private StartOfGasDayParser getStartOfGasDayParser() {
        return startOfGasDayParser;
    }
}
