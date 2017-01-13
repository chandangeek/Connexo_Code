package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.events.XemexWatchTalkEventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.XemexWatchTalkMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging.XemexWatchTalkMessaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.profiles.WatchTalkLoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Protocol class for teh Xemex WatchTalk NTA 2.1 device
 * @author sva
 * @since 18/03/14 - 15:55
 */
public class WatchTalk extends AbstractSmartNtaProtocol {

    private Dsmr40LoadProfileBuilder loadProfileBuilder;
    private Dsmr23Messaging messageProtocol;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final PropertySpecService propertySpecService;

    public WatchTalk(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, PropertySpecService propertySpecService) {
        this.calendarFinder = calendarFinder;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        if (getCache() == null) {
            setCache(new DLMSCache());
        }
        if ((((DLMSCache) getCache()).getObjectList() == null) || ((WatchTalkProperties) getProperties()).getForcedToReadCache()) {
            getLogger().info(((WatchTalkProperties) getProperties()).getForcedToReadCache() ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
            requestConfiguration();
            UniversalObject[] instantiatedObjectList = removeInvalidReferencesFromObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            ((DLMSCache) getCache()).saveObjectList(instantiatedObjectList);
        } else {
            getLogger().info("Cache exist, will not be read!");
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getCache()).getObjectList());
        }
    }

    /**
     * Method to filter out invalid UniversalObjects from the object list <br></br>
     * Apparently the object list contains a dummy object (having all fields 255) as last element - this dummy should be filtered out.
     *
     * @param instantiatedObjectList the object list to handle
     * @return the object list, without dummy objects
     */
    private UniversalObject[] removeInvalidReferencesFromObjectList(UniversalObject[] instantiatedObjectList) {
        List<UniversalObject> result = new ArrayList<>(instantiatedObjectList.length);

        for (UniversalObject object : instantiatedObjectList) {
            if (object.getClassID() == 255 && object.getObisCode().equals(ObisCode.fromString("255.255.255.255.255.255"))) {
                // These should be filtered out
            } else {
                result.add(object);
            }
        }
        return result.toArray(new UniversalObject[0]);
    }

    @Override
    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            loadProfileBuilder = new WatchTalkLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new XemexWatchTalkMessaging(new XemexWatchTalkMessageExecutor(this, this.calendarFinder, this.calendarExtractor, this.messageFileExtractor));
        }
        return messageProtocol;
    }

    @Override
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new XemexWatchTalkEventProfile(this);
        }
        return this.eventProfile;
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        // We need a custom implementation, cause deviation bytes should contain positive winter timezone offset
        // (thus for Europe/Paris always +60, instead of typical -60 in winter / -120 in summer).
        // Or we can avoid this by sending a AXDRDateTime in zone GMT (thus offset 0)
        try {
            this.dlmsSession.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(convertDateToGMTDateTime(newMeterTime));
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not set the Clock object." + e);
        }
    }

    /**
     * Convert a given date to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time  the {@link java.util.Date} to convert
     * @return the AXDRDateTime of the given time
     */
    private AXDRDateTime convertDateToGMTDateTime(Date time) {
        return convertUnixToDateTime(time, TimeZone.getTimeZone("GMT"));
    }

    private AXDRDateTime convertUnixToDateTime(Date time, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(time.getTime());
        return new AXDRDateTime(cal.getTime(), timeZone);
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Positive;
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-08-26 14:01:32 +0200 (Wed, 26 Aug 2015) $";
    }

    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new WatchTalkProperties(this.propertySpecService);
        }
        return this.properties;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }
}