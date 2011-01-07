package com.energyict.genericprotocolimpl.nta.elster.profiles;

import com.energyict.dlms.DataContainer;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.genericprotocolimpl.nta.elster.logs.DisconnectControlLog;
import com.energyict.genericprotocolimpl.nta.elster.logs.EventsLog;
import com.energyict.genericprotocolimpl.nta.elster.logs.FraudDetectionLog;
import com.energyict.genericprotocolimpl.nta.elster.logs.PowerFailureLog;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

/**
 * <p>
 * Copyrights EnergyICT
 * Date: 3-jun-2010
 * Time: 10:16:13
 * </p>
 */
public class EventProfile extends com.energyict.genericprotocolimpl.nta.profiles.EventProfile {

    public EventProfile(AbstractNTAProtocol webrtu) {
        super(webrtu);
    }

    /**
     * Fetch all the events and store them in the {@link com.energyict.genericprotocolimpl.common.StoreObject}
     * @throws IOException
     */
    @Override
    public void getEvents() throws IOException {

        ProfileData profileData = new ProfileData();

        Date lastLogReading = webrtu.getMeter().getLastLogbook();
        if (lastLogReading == null) {
            lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(webrtu.getMeter());
        }
        Calendar fromCal = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCal.setTime(lastLogReading);
        webrtu.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + webrtu.getSerialNumber() + ".");
        DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
        DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
        DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());
        DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(fromCal, webrtu.getToCalendar());

        EventsLog standardEvents = new EventsLog(webrtu.getTimeZone(), dcEvent);
        FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(webrtu.getTimeZone(), dcFraudDetection);
        DisconnectControlLog disconnectControl = new DisconnectControlLog(webrtu.getTimeZone(), dcControlLog);
        PowerFailureLog powerFailure = new PowerFailureLog(webrtu.getTimeZone(), dcPowerFailure);

        profileData.getMeterEvents().addAll(standardEvents.getMeterEvents());
        profileData.getMeterEvents().addAll(fraudDetectionEvents.getMeterEvents());
        profileData.getMeterEvents().addAll(disconnectControl.getMeterEvents());
        profileData.getMeterEvents().addAll(powerFailure.getMeterEvents());

        // Don't create statusbits from the events
//			profileData.applyEvents(webrtu.getMeter().getIntervalInSeconds()/60);

        webrtu.getStoreObject().add(profileData, getMeter());
    }
}
