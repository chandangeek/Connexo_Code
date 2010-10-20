package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.*;

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
     *
     * @return
     */
    private String getChannelObjectId() {
        return getProperties().getChannelConfig().getChannelObjectId(getChannelIndex() - 1);
    }

    /**
     *
     * @return
     */
    private int getChannelIndex() {
        return getMeterChannel().getLoadProfileIndex();
    }

    public ProfileData getProfileData() throws CTRException {
        if (getChannelObjectId() == null) {
            getLogger().warning("No channel config found for channel with loadProfileIndex [" + getChannelIndex() + "]");
            return new ProfileData();
        }

        getIntervalData();

        return new ProfileData();
    }

    private void getIntervalData() throws CTRException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        Calendar fromCalendar = getFromCalendar();
        intervalDatas.addAll(getIntervalDataBlock(getChannelObjectId(), fromCalendar));
    }

    private List<IntervalData> getIntervalDataBlock(String channelId, Calendar fromCalendar) throws CTRException {
        CTRObjectID objectID = new CTRObjectID(channelId);
        PeriodTrace period = new PeriodTrace(1);
        ReferenceDate referenceDate = new ReferenceDate().parse(fromCalendar);
        Trace_CQueryResponseStructure response = getRequestFactory().queryTrace_C(objectID, period, referenceDate);
        return getIntervalDatasFromResponse(response);
    }

    private List<IntervalData> getIntervalDatasFromResponse(Trace_CQueryResponseStructure response) {
        response.getDate().getCalendar(getDeviceTimeZone());

        for (AbstractCTRObject object : response.getTraceData()) {
            if (!object.getQlf().isInvalid()) {
                System.out.println(object.getValue()[0].getValue());
            }
        }
        return new ArrayList<IntervalData>();
    }

    private Calendar getFromCalendar() {
        Date lastReading = getMeterChannel().getLastReading();
        if (lastReading == null) {
            lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getRtu());
        }
        Calendar cal = ProtocolUtils.getCleanCalendar(getDeviceTimeZone());
        cal.setTime(lastReading);
        return cal;
    }

    private void getProfileInfo(String... profiles) throws CTRException {
        List<AbstractCTRObject> ctrObjectList = getRequestFactory().queryRegisters(AttributeType.getValueOnly(), profiles);
        for (AbstractCTRObject ctrObject : ctrObjectList) {
            System.out.println(ctrObject);
        }
    }

}
