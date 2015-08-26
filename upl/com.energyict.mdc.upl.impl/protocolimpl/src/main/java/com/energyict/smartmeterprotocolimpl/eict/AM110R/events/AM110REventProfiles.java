package com.energyict.smartmeterprotocolimpl.eict.AM110R.events;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110RRegisterFactory;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.BasicEventLog;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.EventUtils;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.HANDeviceEventLog;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.ManufacturerSpecificEventLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 8/08/11
 * Time: 8:35
 */
public class AM110REventProfiles {

    private final AM110R meterProtocol;

    public AM110REventProfiles(AM110R meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<MeterEvent> getEvents(Date from) throws IOException {
        ArrayList<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        getLogger().log(Level.INFO, "Reading EVENTS from AM110R with serial number " + getMeterProtocol().getProperties().getSerialNumber() + ".");

        if(from == null){
            from = ProtocolUtils.getClearLastMonthDate(this.meterProtocol.getTimeZone());
        }
        Calendar fromCalendar = getFromCalendar(from);

        int logbookSelectorBitMask = meterProtocol.getProperties().getLogbookSelector();
        meterEvents.addAll(((logbookSelectorBitMask &  0x01) == 0x01) ? getStandardEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x02) == 0x02) ? getFraudDetectionEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x04) == 0x04) ? getFirmwareEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x08) == 0x08) ? getPowerQualityEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x10) == 0x10) ? getCommunicationsFailureEventLog(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x20) == 0x20) ? getHANDeviceEventLogbook(fromCalendar) : new ArrayList<MeterEvent>());
        meterEvents.addAll(((logbookSelectorBitMask &  0x40) == 0x40) ? getManufacturerLogbook(fromCalendar) : new ArrayList<MeterEvent>());
        EventUtils.removeDuplicateEvents(meterEvents);
        EventUtils.removeStoredEvents(meterEvents, fromCalendar.getTime());        
        return meterEvents;
    }

    private Calendar getFromCalendar(Date from) {
        Calendar fromCal = ProtocolUtils.getCleanCalendar(getMeterProtocol().getTimeZone());
        fromCal.setTime(from);
        return fromCal;
    }

    private Logger getLogger() {
        return getMeterProtocol().getLogger();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getMeterProtocol().getDlmsSession().getCosemObjectFactory();
    }

    public AM110R getMeterProtocol() {
        return meterProtocol;
    }

    private List<MeterEvent> getStandardEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                AM110RRegisterFactory.STANDARD_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFraudDetectionEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                AM110RRegisterFactory.FRAUD_DETECTION_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getFirmwareEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                AM110RRegisterFactory.FIRMWARE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getPowerQualityEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                AM110RRegisterFactory.POWER_QUALITY_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getCommunicationsFailureEventLog(Calendar from) throws IOException {
        BasicEventLog basicEventLog = new BasicEventLog(
                AM110RRegisterFactory.COMM_FAILURE_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return basicEventLog.getEvents(from);
    }

    private List<MeterEvent> getHANDeviceEventLogbook(Calendar from) throws IOException {
        HANDeviceEventLog hanDeviceEventLog = new HANDeviceEventLog(
                AM110RRegisterFactory.HAN_MANAGEMENT_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return hanDeviceEventLog.getEvents(from);
    }

    private List<MeterEvent> getManufacturerLogbook(Calendar from) throws IOException {
        ManufacturerSpecificEventLog manufacturerSpecificEventLog = new ManufacturerSpecificEventLog(
                AM110RRegisterFactory.MANUFACTURER_SPECIFIC_EVENT_LOG,
                getCosemObjectFactory(),
                getLogger()
        );
        return manufacturerSpecificEventLog.getEvents(from);
    }
}
