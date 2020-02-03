package com.energyict.protocolimplv2.nta.abstractnta.profiles;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
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
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.common.topology.DeviceMapping;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.MASTER;
import static com.energyict.protocolimplv2.nta.abstractnta.profiles.AbstractNtaLogBookFactory.MeterType.SLAVE;

public abstract class AbstractNtaLogBookFactory<T extends AbstractSmartNtaProtocol> implements DeviceLogBookSupport {

    public enum MeterType { MASTER, SLAVE;

        public boolean isMaster() {
            return MASTER==this;
        }
    }

    protected static final ObisCode STANDARD_EVENT_LOG =                ObisCode.fromString("0.0.99.98.0.255");
    protected static final ObisCode POWER_FAILURE_LOG =                 ObisCode.fromString("1.0.99.97.0.255");
    protected static final ObisCode FRAUD_DETECTION_LOG =               ObisCode.fromString("0.0.99.98.1.255");
    protected static final ObisCode CONTROL_LOG =                       ObisCode.fromString("0.0.99.98.2.255");
    protected static final ObisCode MBUS_EVENT_LOG =                    ObisCode.fromString("0.0.99.98.3.255");
    protected static final ObisCode MBUS_CONTROL_LOG =                  ObisCode.fromString("0.x.24.5.0.255");
    protected static final ObisCode COMMUNICATION_SESSION_EVENT_LOG =   ObisCode.fromString("0.0.99.98.4.255");
    protected static final ObisCode VOLTAGE_QUALITY_LOG =               ObisCode.fromString("0.0.99.98.5.255");

    private T protocol;
    protected final CollectedDataFactory collectedDataFactory;
    protected final IssueFactory issueFactory;
    private List<DeviceMapping> mbusMeterMap;

    public AbstractNtaLogBookFactory(T protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        mbusMeterMap = protocol.getMeterTopology().getMbusMeterMap();
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            ObisCode physicalAddressCorrectedObisCode = getPhysicalAddressCorrectedObisCode(logBookReader.getLogBookObisCode(), logBookReader.getMeterSerialNumber());
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            MeterType meterType = determineMeterTypeForSerialNumber(logBookReader.getMeterSerialNumber());
            if (isSupported(physicalAddressCorrectedObisCode, meterType)) {
                ProfileGeneric profileGeneric = null;
                try {
                    //needs to be set, otherwise day of week field is set incorrectly
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(physicalAddressCorrectedObisCode, getProtocol().useDsmr4SelectiveAccessFormat());
                } catch (NotInObjectListException e) {
                    getProtocol().journal(Level.WARNING, "Cannot get object for "+physicalAddressCorrectedObisCode+": "+e.getLocalizedMessage());
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }

                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());
                    DataContainer dataContainer;
                    try {
                        getProtocol().journal("Reading logbook "+profileGeneric.getObisCode()+" from "+fromDate.getTime());
                        dataContainer = profileGeneric.getBuffer(fromDate, getCalendar());
                        getProtocol().journal("Collected "+dataContainer.getRoot().element.length+" raw events from "+profileGeneric.getObisCode());
                        collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer,  logBookReader));
                    } catch (NotInObjectListException e) {
                        getProtocol().journal(Level.WARNING, "Logbook not in objects list: "+profileGeneric.getObisCode().toString());
                        collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                    } catch (IOException e) {
                        getProtocol().journal(Level.WARNING, "I/O exception while reading logbook: "+logBookReader.getLogBookObisCode().toString());
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                            collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, String.format("Logbook with OBIS code '%s'' is not supported by the protocol", logBookReader.getLogBookObisCode().toString()), logBookReader.getLogBookObisCode().toString()));
                        }
                    }
                }
            } else {
                getProtocol().journal(Level.WARNING, "Logbook not supported by the protocol: "+physicalAddressCorrectedObisCode.toString());
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, String.format("Logbook with OBIS code '%s'' is not supported by the protocol", logBookReader.getLogBookObisCode().toString()), logBookReader.getLogBookObisCode().toString()));
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
            meterEvents = parseStandardEventLog(dataContainer);
        } else if (logBookObisCode.equals(CONTROL_LOG)) {
            getProtocol().journal("Parsing as disconnect control log");
            meterEvents = parseControlLog(dataContainer);
        } else if (logBookObisCode.equals(POWER_FAILURE_LOG)) {
            getProtocol().journal("Parsing as power failure log");
            meterEvents = parsePowerFailureLog(dataContainer);
        } else if (logBookObisCode.equals(FRAUD_DETECTION_LOG)) {
            getProtocol().journal("Parsing as fraud detection log");
            meterEvents = parseFraudDetectionLog(dataContainer);
        }  else if (logBookObisCode.equals(COMMUNICATION_SESSION_EVENT_LOG)) {
            getProtocol().journal("Parsing as communication sessions event log");
            meterEvents = parseCommunicationLogEventLog(dataContainer);
        }  else if (logBookObisCode.equals(VOLTAGE_QUALITY_LOG)) {
            getProtocol().journal("Parsing as voltage quality log");
            meterEvents = parseVoltageQualityLog(dataContainer);
        } else if (logBookObisCode.equalsIgnoreBChannel(MBUS_CONTROL_LOG)) {
            int channel = protocol.getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            getProtocol().journal("Parsing as MBus control log on channel "+ channel);
            meterEvents = parseMBUSControlLog(dataContainer, channel);
        }  else if (logBookObisCode.equalsIgnoreBChannel(MBUS_EVENT_LOG)) {
            int channel = protocol.getPhysicalAddressFromSerialNumber(logBookReader.getMeterSerialNumber());
            getProtocol().journal("Parsing as MBus event log on channel " + channel);
            meterEvents = parseMBUSEventLog(dataContainer, channel);
        } else {
            getProtocol().journal("Logbook " + logBookObisCode + " not supported by protocol");
            return new ArrayList<>();
        }
        getProtocol().journal("Decoded "+meterEvents.size()+" events from "+logBookObisCode);
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    private MeterType determineMeterTypeForSerialNumber(String serialNumber) {
        for (DeviceMapping deviceMapping : mbusMeterMap) {
            if (deviceMapping.getSerialNumber().equals(serialNumber)) {
                return SLAVE;
            }
        }
        return MASTER;
    }

    protected List<Integer> getUsedMBUSChannels() {
        List<Integer> usedMbusChannels = new ArrayList<>();
        for (DeviceMapping deviceMapping: mbusMeterMap) {
            usedMbusChannels.add(deviceMapping.getPhysicalAddress());
        }
        return usedMbusChannels;
    }

    protected T getProtocol() {
        return protocol;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

    protected abstract List<MeterEvent> parseStandardEventLog(DataContainer dataContainer) throws ProtocolException;
    protected abstract List<MeterEvent> parseControlLog(DataContainer dataContainer) throws ProtocolException;
    protected abstract List<MeterEvent> parsePowerFailureLog(DataContainer dataContainer) throws ProtocolException;
    protected abstract List<MeterEvent> parseFraudDetectionLog(DataContainer dataContainer) throws ProtocolException;
    protected abstract List<MeterEvent> parseCommunicationLogEventLog(DataContainer dataContainer) throws ProtocolException;
    protected abstract List<MeterEvent> parseMBUSControlLog(DataContainer dataContainer, int channel) throws ProtocolException;
    protected abstract List<MeterEvent> parseMBUSEventLog(DataContainer dataContainer, int channel) throws ProtocolException;
    protected abstract List<MeterEvent> parseVoltageQualityLog(DataContainer dataContainer) throws ProtocolException;
    protected abstract ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String meterSerialNumber);

    protected abstract boolean isSupported(ObisCode obisCode, MeterType meterType);


}
