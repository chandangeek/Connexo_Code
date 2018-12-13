package com.energyict.smartmeterprotocolimpl.elster.AS300P.eventhandling;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.EventUtils;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300P;
import com.energyict.smartmeterprotocolimpl.elster.AS300P.AS300PObisCodeProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides functionality to collect the events from the device and return them as a list of {@link com.energyict.protocol.MeterEvent}
 * Copyrights EnergyICT
 * Date: 2-dec-2010
 * Time: 13:01:44
 */
public class AS300PEventProfiles {

    private final AS300P meterProtocol;

    /**
     * Default constructor
     *
     * @param meterProtocol the meterProtocol to create events for
     */
    public AS300PEventProfiles(AS300P meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Getter for the MeterEvent list
     *
     * @param lastLogbookDate the time to start collection events from the device
     * @return a list of MeterEvents
     */
    public List<MeterEvent> getEventLog(Date lastLogbookDate) throws IOException {
        Calendar fromCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
        fromCalendar.setTime(lastLogbookDate == null ? new Date(0) : lastLogbookDate);
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        int logbookSelectorBitMask = meterProtocol.getProperties().getLogbookSelector();
        meterEvents.addAll(((logbookSelectorBitMask & 0x01) == 0x01) ? getStandardEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x02) == 0x02) ? getFraudEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x04) == 0x04) ? getDisconnectControlEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x08) == 0x08) ? getFirmwareEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x10) == 0x10) ? getPowerQualityEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x20) == 0x20) ? getCommunicationEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x40) == 0x40) ? getPrepaymentEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x80) == 0x80) ? getPowerFailureEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x100) == 0x100) ? getCOTSEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x200) == 0x200) ? getSynchronisationEvents(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask & 0x400) == 0x400) ? getTariffUpdateEvents(fromCalendar) : new ArrayList<MeterEvent>());
        EventUtils.removeDuplicateEvents(meterEvents);
        EventUtils.removeStoredEvents(meterEvents, fromCalendar.getTime());
        return meterEvents;
    }

    protected List<MeterEvent> getStandardEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.StandardEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFraudEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.FraudDetectionEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getDisconnectControlEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.DisconnectControlLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFirmwareEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.FirmwareEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPowerQualityEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.PowerQualityEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPowerFailureEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.PowerFailureEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCommunicationEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.CommunicationsEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getTariffUpdateEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.TariffUpdateEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getSynchronisationEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.SynchronisationEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPrepaymentEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.PrepaymentEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCOTSEvents(Calendar from) throws IOException {
        AS300PEventLog basicEventLog = new AS300PEventLog(
                AS300PObisCodeProvider.COTSEventLogObisCode,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    /**
     * Getter for the {@link #meterProtocol}
     *
     * @return the {@link #meterProtocol}
     */
    private AS300P getMeterProtocol() {
        return this.meterProtocol;
    }

    /**
     * Getter for the AS300's cosemObjectFactory
     *
     * @return the CosemObjectFactory of the AS300
     */
    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    /**
     * The protocol logger
     *
     * @return the protocol logger object
     */
    private Logger getLogger() {
        return getMeterProtocol().getLogger();
    }
}