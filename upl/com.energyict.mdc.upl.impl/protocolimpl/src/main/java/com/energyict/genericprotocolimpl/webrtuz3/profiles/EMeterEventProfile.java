package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.EventsLog;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author gna
 */
public class EMeterEventProfile {

    private static final ObisCode EVENT_LOG_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    private EDevice eDevice;

    /**
     * Constructor to initialize the eDevice
     *
     * @param eDevice
     */
    public EMeterEventProfile(EDevice eDevice) {
        this.eDevice = eDevice;
    }

    /**
     * Read the events from the eMeter
     *
     * @return
     * @throws IOException
     */
    public ProfileData getEvents() throws IOException {

        try {

            ProfileData profileData = new ProfileData();

            Date lastLogReading = eDevice.getMeter().getLastLogbook();
            if (lastLogReading == null) {
                lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(eDevice.getMeter());
            }
            Calendar fromCal = ProtocolUtils.getCleanCalendar(this.eDevice.getTimeZone());
            fromCal.setTime(lastLogReading);
            eDevice.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + eDevice.getSerialNumber() + ".");

            ObisCode eventLogObisCode = getCorrectedObisCode(EVENT_LOG_OBISCODE);
            DataContainer eventsLogDataContainer = getLogAsDataContainer(eventLogObisCode, fromCal);
            EventsLog standardEvents = new EventsLog(eventsLogDataContainer);

            profileData.getMeterEvents().addAll(standardEvents.getMeterEvents());
            return profileData;

        } catch (Exception e) {
            e.printStackTrace();
            throw new NestedIOException(e);
        }

    }

    /**
     * Get the corrected obisCode field to match the physical address of the eMeter
     *
     * @param obisCode
     * @return
     */
    private ObisCode getCorrectedObisCode(ObisCode obisCode) {
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) eDevice.getPhysicalAddress());
    }

    /**
     * Read the logbook as DataContainer
     *
     * @param obisCode
     * @param from
     * @return
     * @throws IOException
     */
    private DataContainer getLogAsDataContainer(ObisCode obisCode, Calendar from) throws IOException {
        return getCosemObjectFactory().getProfileGeneric(obisCode).getBuffer(from, eDevice.getToCalendar());
    }

    /**
     * Getter for the CosemObjectFactory
     *
     * @return
     */
    private CosemObjectFactory getCosemObjectFactory() {
        return this.eDevice.getCosemObjectFactory();
    }

}
