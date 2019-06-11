package com.energyict.protocolimplv2.nta.dsmr23.profiles;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.common.topology.DeviceMapping;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;
import com.energyict.protocolimplv2.nta.dsmr23.eventhandling.*;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dsmr23LogBookFactory implements DeviceLogBookSupport {

    protected static final ObisCode STANDARD_EVENT_LOG =  ObisCode.fromString("0.0.99.98.0.255");
    protected static final ObisCode POWER_FAILURE_LOG =   ObisCode.fromString("1.0.99.97.0.255");
    protected static final ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    protected static final ObisCode CONTROL_LOG =         ObisCode.fromString("0.0.99.98.2.255");
    protected static final ObisCode MBUS_EVENT_LOG =      ObisCode.fromString("0.x.99.98.3.255");
    protected static final ObisCode MBUS_CONTROL_LOG =    ObisCode.fromString("0.x.24.5.0.255");
    protected AbstractDlmsProtocol protocol;

    /**
     * List of obiscodes of the supported log books
     */
    protected List<ObisCode> supportedLogBooks;
    protected final CollectedDataFactory collectedDataFactory;
    protected final IssueFactory issueFactory;

    public Dsmr23LogBookFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        initializeSupportedLogBooks();
    }

    public void initializeSupportedLogBooks() {
        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
        supportedLogBooks.add(POWER_FAILURE_LOG);
        supportedLogBooks.add(FRAUD_DETECTION_LOG);
        addControlLogBooksIfSupported();
        addMbusLogBooksIfSupported();
    }

    private void addControlLogBooksIfSupported() {
        if (protocol.hasBreaker()){
            supportedLogBooks.add(CONTROL_LOG);
        }
    }

    private void addMbusLogBooksIfSupported() {
        supportedLogBooks.add(MBUS_EVENT_LOG);
        for (DeviceMapping mbusMeter : ((MeterTopology) protocol.getMeterTopology()).getMbusMeterMap()) {
            //for mbus control log
            ObisCode correctedObisCode = ProtocolTools.setObisCodeField(MBUS_CONTROL_LOG, 1, (byte) mbusMeter.getPhysicalAddress());
            supportedLogBooks.add(correctedObisCode);
            //for mbus event log
            correctedObisCode = ProtocolTools.setObisCodeField(MBUS_EVENT_LOG, 1, (byte) mbusMeter.getPhysicalAddress());
            supportedLogBooks.add(correctedObisCode);
        }
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
             if (isSupported(logBookReader)) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber()));
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());
                    DataContainer dataContainer;
                    try {
                        getProtocol().journal("Reading logbook "+profileGeneric.getObisCode()+" from "+fromDate.getTime());
                        dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                        collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer,  logBookReader));
                    } catch (NotInObjectListException e) {
                        getProtocol().journal(Level.WARNING, "Logbook not in objects list: "+logBookReader.getLogBookObisCode().toString());
                        collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                            getProtocol().journal(Level.WARNING, "Logbook not supported: "+logBookReader.getLogBookObisCode().toString());
                            collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, LogBookReader logBookReader) throws ProtocolException {
        ObisCode logBookObisCode = logBookReader.getLogBookObisCode();
        getProtocol().journal("Parsing logbook "+logBookObisCode);
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            getProtocol().journal("Parsing as standard event log");
            meterEvents = new EventsLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(CONTROL_LOG)) {
            getProtocol().journal("Parsing as disconnect control log");
            meterEvents = new DisconnectControlLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(POWER_FAILURE_LOG)) {
            getProtocol().journal("Parsing as power failure log");
            meterEvents = new PowerFailureLog(dataContainer).getMeterEvents();
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            getProtocol().journal("Parsing as fraud detection log");
            meterEvents = new FraudDetectionLog(dataContainer).getMeterEvents();
        }else if (logBookObisCode.equalsIgnoreBChannel(MBUS_CONTROL_LOG)) {
            getProtocol().journal("Parsing as MBus control log");
            meterEvents = new MbusControlLog(dataContainer).getMeterEvents();
        }  else if (logBookObisCode.equalsIgnoreBChannel(MBUS_EVENT_LOG)) {
            int channel = protocol.getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            getProtocol().journal("Parsing as MBus event log on channel "+channel);
            meterEvents = new MbusLog(dataContainer, channel).getMeterEvents();
        } else {
            getProtocol().journal("Logbook " + logBookObisCode + " not supported by protocol");
            return new ArrayList<>();
        }

        DlmsSessionProperties props = this.getProtocol().getDlmsSessionProperties();
        if(props instanceof Dsmr23Properties){
            Dsmr23Properties properties = (Dsmr23Properties) props;
            if(properties.getFrameCounterLimit() != 0 && properties.replayAttackPreventionEnabled()){
                checkFrameCounterEvents(meterEvents);
            }
        }
        getProtocol().journal("Decoded "+meterEvents.size()+" events from "+logBookObisCode);
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    protected void checkFrameCounterEvents(List<MeterEvent> eventList) {
        SecurityContext securityContext = protocol.getDlmsSession().getAso().getSecurityContext();

        generateFrameCounterLimitEvent(securityContext.getFrameCounter(), "Frame Counter", 900, eventList);

        if (securityContext.getResponseFrameCounter() != 0) {
            generateFrameCounterLimitEvent(securityContext.getResponseFrameCounter(),"Response Frame Counter", 901, eventList);
        } else {
            getProtocol().journal("Response frame counter not initialized.");
        }

    }

    protected void generateFrameCounterLimitEvent(long frameCounter, String name, int eventId, List<MeterEvent> eventList) {
        try {
            long frameCounterLimit = ((Dsmr23Properties)this.getProtocol().getDlmsSessionProperties()).getFrameCounterLimit();

            if (frameCounterLimit==0){
                getProtocol().journal("Frame counter threshold not configured. FYI the current "+name+" is "+frameCounter);
            } else if (frameCounter>frameCounterLimit) {
                getProtocol().journal(name+": " + frameCounter + " is above the threshold ("+frameCounterLimit+") - will create an event");
                MeterEvent frameCounterEvent = new MeterEvent(new Date(), 0, eventId, name+" above threshold: " + frameCounter);
                eventList.add(frameCounterEvent);
            } else {
                getProtocol().journal(name + " below configured threshold: "+frameCounter+" < "+frameCounterLimit);
            }
        }catch (Exception e) {
            getProtocol().journal(Level.WARNING, "Error getting  "+name+e.getMessage(), e);
        }
    }


    protected boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBookObisCode : supportedLogBooks) {
            if (supportedLogBookObisCode.equals(protocol.getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber()))) {
                return true;
            }
        }
        return false;
    }

    protected DLMSMeterConfig getMeterConfig() {
        return this.protocol.getDlmsSession().getMeterConfig();
    }

    protected Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

    public AbstractDlmsProtocol getProtocol() {
        return protocol;
    }
}