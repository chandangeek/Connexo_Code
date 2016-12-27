package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author gna
 */
public class EMeterEventProfile {

    private static final ObisCode EVENT_LOG_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    private final SimpleMeter simpleMeter;
    private final DlmsSession dlmsSession;

    /**
     * Constructor to initialize the eventProfile
     *
     * @param simpleMeter the meter for who the eventProfile is intended for
     * @param dlmsSession
     */
    public EMeterEventProfile(SimpleMeter simpleMeter, DlmsSession dlmsSession) {
        this.simpleMeter = simpleMeter;
        this.dlmsSession = dlmsSession;
    }

    /**
     * Read the events from the eMeter
     *
     * @return
     * @throws java.io.IOException
     */
    public List<MeterEvent> getEvents(Date lastLogbookDate) throws IOException {

        try {

            if (lastLogbookDate == null) {
                lastLogbookDate = ProtocolUtils.getClearLastMonthDate(this.simpleMeter.getTimeZone());
            }
            Calendar fromCal = ProtocolUtils.getCleanCalendar(this.simpleMeter.getTimeZone());
            fromCal.setTime(lastLogbookDate);
            this.simpleMeter.getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + this.simpleMeter.getSerialNumber() + ".");

            ObisCode eventLogObisCode = getCorrectedObisCode(EVENT_LOG_OBISCODE);
            DataContainer eventsLogDataContainer = getLogAsDataContainer(eventLogObisCode, fromCal);
            EventsLog standardEvents = new EventsLog(eventsLogDataContainer);

            return standardEvents.getMeterEvents();

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
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) simpleMeter.getPhysicalAddress());
    }

    /**
     * Read the logbook as DataContainer
     *
     * @param obisCode
     * @param from
     * @return
     * @throws java.io.IOException
     */
    private DataContainer getLogAsDataContainer(ObisCode obisCode, Calendar from) throws IOException {
        return getCosemObjectFactory().getProfileGeneric(obisCode).getBuffer(from);
    }

    /**
     * Getter for the CosemObjectFactory
     *
     * @return
     */
    private CosemObjectFactory getCosemObjectFactory() {
        return this.dlmsSession.getCosemObjectFactory();
    }

}
