package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.Connection;
import com.energyict.protocolimplv2.elster.garnet.common.GPRSConnection;
import com.energyict.protocolimplv2.elster.garnet.common.ReadingResponse;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.UnableToExecuteException;
import com.energyict.protocolimplv2.elster.garnet.exception.UnexpectedResponseException;
import com.energyict.protocolimplv2.elster.garnet.frame.RequestFrame;
import com.energyict.protocolimplv2.elster.garnet.frame.ResponseFrame;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.ExtendedFunction;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Function;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Part;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorStatusRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorStatusResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ContactorRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ContactorResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverMetersRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverMetersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverRepeatersRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverRepeatersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.LogBookEventRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.LogBookEventResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.OpenSessionRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.OpenSessionResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.RadioParametersRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.RadioParametersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ReadingRequestStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ReadingResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ContactorMode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ReadingSelector;
import com.energyict.protocolimplv2.elster.garnet.structure.field.RepeaterDiagnostic;
import com.energyict.protocolimplv2.elster.garnet.structure.field.SessionKeyPart;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 15:02
 */
public class RequestFactory {

    private static final int NR_OF_REPEATERS_PER_PART = 40;
    private static final int REMOTE_CONCENTRATOR_DEVICE_ID = 0;
    private static final int NR_OF_METERS_PER_REGISTER_READING_RESPONSE = 6;

    private Connection connection;
    private ComChannel comChannel;
    private GarnetProperties properties;
    private final PropertySpecService propertySpecService;

    /**
     * Map containing cached data that is already read out during this communication session.
     * This map can be used in order to prevent the same data has to be read out twice from the device.
     */
    private Map<String, Data> cachedData;

    public RequestFactory(PropertySpecService propertySpecService) {
        this.cachedData = new HashMap<>();
        this.propertySpecService = propertySpecService;
    }

    public ConcentratorVersionResponseStructure readConcentratorVersion() throws GarnetException {
        ConcentratorVersionResponseStructure responseData = (ConcentratorVersionResponseStructure) loadResponseDataFromCache(FunctionCode.CONCENTRATOR_VERSION_RESPONSE.name());
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getConcentratorVersionRequest(getDeviceIdOfMaster())
            );

            if (response.getData() instanceof ConcentratorVersionResponseStructure) {
                responseData = (ConcentratorVersionResponseStructure) response.getData();
                cachedData.put(FunctionCode.CONCENTRATOR_VERSION_RESPONSE.name(), responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected ConcentratorVersionResponseStructure but was " + response.getData().getClass().getSimpleName())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getConcentratorVersionRequest(int destinationAddress) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.CONCENTRATOR_VERSION_REQUEST));
        request.setData(new ConcentratorVersionRequestStructure(this));
        request.generateAndSetCRC();
        return request;
    }

    public ConcentratorStatusResponseStructure readConcentratorStatus() throws GarnetException {
        ConcentratorStatusResponseStructure responseData = (ConcentratorStatusResponseStructure) loadResponseDataFromCache(FunctionCode.CONCENTRATOR_STATUS_RESPONSE.name());
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getConcentratorStatusRequest(getDeviceIdOfMaster())
            );

            if (response.getData() instanceof ConcentratorStatusResponseStructure) {
                responseData = (ConcentratorStatusResponseStructure) response.getData();
                cachedData.put(FunctionCode.CONCENTRATOR_STATUS_RESPONSE.name(), responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected ConcentratorStatusResponseStructure but was " + response.getData().getClass().getSimpleName())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getConcentratorStatusRequest(int destinationAddress) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.CONCENTRATOR_STATUS_REQUEST));
        request.setData(new ConcentratorStatusRequestStructure(this));
        request.generateAndSetCRC();
        return request;
    }

    public RadioParametersResponseStructure readRadioParameters() throws GarnetException {
        RadioParametersResponseStructure responseData = (RadioParametersResponseStructure) loadResponseDataFromCache(FunctionCode.RADIO_PARAMETERS_RESPONSE.name());
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getRadioParametersRequest(getDeviceIdOfMaster())
            );

            if (response.getData() instanceof RadioParametersResponseStructure) {
                responseData = (RadioParametersResponseStructure) response.getData();
                cachedData.put(FunctionCode.RADIO_PARAMETERS_RESPONSE.name(), responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected RadioParametersResponseStructure but was " + response.getData().getClass().getSimpleName())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getRadioParametersRequest(int destinationAddress) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.RADIO_PARAMETERS_REQUEST));
        request.setData(new RadioParametersRequestStructure(this));
        request.generateAndSetCRC();
        return request;
    }

    public void openSession() throws GarnetException {
        RequestFrame openSessionRequest = getOpenSessionRequest(REMOTE_CONCENTRATOR_DEVICE_ID);
        ResponseFrame response = getConnection().sendFrameGetResponse(openSessionRequest);

        if (response.getData() instanceof OpenSessionResponseStructure) {
            SessionKeyPart firstPartOfSessionKey = ((OpenSessionRequestStructure) openSessionRequest.getData()).getFirstPartOfSessionKey();
            SessionKeyPart secondPartOfSessionKey = ((OpenSessionResponseStructure) response.getData()).getSecondPartOfSessionKey();

            byte[] sessionKey = ProtocolTools.concatByteArrays(firstPartOfSessionKey.getBytes(), secondPartOfSessionKey.getBytes());
            getConnection().setSessionKey(sessionKey);
        } else {
            throw CommunicationException.unexpectedResponse(
                    new UnexpectedResponseException("Expected OpenSessionResponseStructure but was " + response.getData().getClass().getSimpleName())
            );
        }
    }

    protected RequestFrame getOpenSessionRequest(int destinationAddress) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.OPEN_SESSION_REQUEST));
        request.setData(new OpenSessionRequestStructure(this));
        request.generateAndSetCRC();
        return request;
    }

    public Map<Integer, RepeaterDiagnostic> discoverRepeaters() throws GarnetException {
        Map<Integer, RepeaterDiagnostic> repeaterDiagnosticMap = new HashMap<>();

        for(int i = 1; i < 18; i++) {
            DiscoverRepeatersResponseStructure discoveredRepeatersStructure = discoverRepeaters(i);
            HashMap<Address, RepeaterDiagnostic> repeaterMap = discoveredRepeatersStructure.getRepeaterMap();
            for (Map.Entry<Address, RepeaterDiagnostic> entry : repeaterMap.entrySet()) {
                repeaterDiagnosticMap.put(entry.getKey().getAddress(), entry.getValue());
            }

            if (repeaterDiagnosticMap.size() < (NR_OF_REPEATERS_PER_PART * i)) {
                return repeaterDiagnosticMap;   // In case response contained less than 40 entries, it is useless to read out the next part (will all be empty)
            }
        }
        return repeaterDiagnosticMap;
    }

    public DiscoverRepeatersResponseStructure discoverRepeaters(int part) throws GarnetException {
        DiscoverRepeatersResponseStructure responseData = (DiscoverRepeatersResponseStructure) loadResponseDataFromCache(FunctionCode.DISCOVER_REPEATERS_RESPONSE.name() + part);
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getDiscoverRepeatersRequest(REMOTE_CONCENTRATOR_DEVICE_ID, part)
            );

            if (response.getData() instanceof DiscoverRepeatersResponseStructure) {
                responseData = (DiscoverRepeatersResponseStructure) response.getData();
                cachedData.put(FunctionCode.DISCOVER_REPEATERS_RESPONSE.name() + part, responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected DiscoverRepeatersResponseStructure but was " + response.getData().getClass().getSimpleName())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getDiscoverRepeatersRequest(int destinationAddress, int part) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.DISCOVER_REPEATERS_REQUEST));
        request.setExtendedFunction(new ExtendedFunction());
        request.setPart(new Part(part));
        request.setData(new DiscoverRepeatersRequestStructure(this));
        request.generateAndSetCRC();
        return request;
    }

    public DiscoverMetersResponseStructure discoverMeters() throws GarnetException {
        DiscoverMetersResponseStructure responseData = (DiscoverMetersResponseStructure) loadResponseDataFromCache(FunctionCode.DISCOVER_METERS_RESPONSE.name());
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getDiscoverMetersRequest(getDeviceIdOfMaster())
            );

            if (response.getData() instanceof DiscoverMetersResponseStructure) {
                responseData = (DiscoverMetersResponseStructure) response.getData();
                cachedData.put(FunctionCode.DISCOVER_METERS_RESPONSE.name(), responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected DiscoverMetersResponseStructure but was " + response.getData().getFunctionCode().name())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getDiscoverMetersRequest(int destinationAddress) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.DISCOVER_METERS_REQUEST));
        request.setData(new DiscoverMetersRequestStructure(this));
        request.generateAndSetCRC();
        return request;
    }

    public ReadingResponse readRegisterReading(int meterIndex, ReadingRequestStructure.ReadingMode mode) throws GarnetException {
        if (meterIndex < 0 || meterIndex > 11) {
            throw new UnableToExecuteException("Failed to read out the register readings - encountered invalid meter index " + meterIndex + "; the index should be between 0 and 12");
        }

        int part = meterIndex < NR_OF_METERS_PER_REGISTER_READING_RESPONSE ? 0 : 1;
        ReadingResponseStructure readingResponseStructure = readRegisterReadings(part, mode);
        return new ReadingResponse(readingResponseStructure, meterIndex % NR_OF_METERS_PER_REGISTER_READING_RESPONSE);
    }

    public ReadingResponseStructure readRegisterReadings(int readingSelector, ReadingRequestStructure.ReadingMode mode) throws GarnetException {
        String cacheKey = mode.equals(ReadingRequestStructure.ReadingMode.CHECKPOINT_READING)
                ? FunctionCode.CHECKPOINT_READING_RESPONSE.name() + readingSelector
                : FunctionCode.ONLINE_READING_RESPONSE.name() + readingSelector;
        ReadingResponseStructure responseData = (ReadingResponseStructure) loadResponseDataFromCache(cacheKey);
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getReadingRequest(getDeviceIdOfMaster(), readingSelector, mode)
            );

            if (response.getData() instanceof ReadingResponseStructure) {
                responseData = (ReadingResponseStructure) response.getData();
                cachedData.put(cacheKey, responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected ReadingResponseStructure but was " + response.getData().getClass().getSimpleName())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getReadingRequest(int destinationAddress, int readingSelector, ReadingRequestStructure.ReadingMode mode) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(mode.equals(ReadingRequestStructure.ReadingMode.CHECKPOINT_READING) ? FunctionCode.CHECKPOINT_READING_REQUEST : FunctionCode.ONLINE_READING_REQUEST));
        ReadingRequestStructure requestStructure = new ReadingRequestStructure(this);
        requestStructure.setReadingSelector(new ReadingSelector(readingSelector));
        request.setData(requestStructure);
        request.generateAndSetCRC();
        return request;
    }

    public LogBookEventResponseStructure readLogBookEvent(int logBookEventNr) throws GarnetException {
        LogBookEventResponseStructure responseData = (LogBookEventResponseStructure) loadResponseDataFromCache(FunctionCode.LOGBOOK_EVENT_RESPONSE.name() + logBookEventNr);
        if (responseData == null) {
            ResponseFrame response = getConnection().sendFrameGetResponse(
                    getLogBookEventRequest(getDeviceIdOfMaster(), logBookEventNr)
            );

            if (response.getData() instanceof LogBookEventResponseStructure) {
                responseData = (LogBookEventResponseStructure) response.getData();
                cachedData.put(FunctionCode.LOGBOOK_EVENT_RESPONSE.name() + logBookEventNr, responseData);
            } else {
                throw CommunicationException.unexpectedResponse(
                        new UnexpectedResponseException("Expected LogBookEventResponseStructure but was " + response.getData().getClass().getSimpleName())
                );
            }
        }
        return responseData;
    }

    protected RequestFrame getLogBookEventRequest(int destinationAddress, int logBookEventNr) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.LOGBOOK_EVENT_REQUEST));
        LogBookEventRequestStructure requestStructure = new LogBookEventRequestStructure(this, logBookEventNr);
        request.setData(requestStructure);
        request.generateAndSetCRC();
        return request;
    }

    public ContactorResponseStructure executeContactorOperation(String serialNumber, boolean isReconnect) throws GarnetException {
        ContactorMode mode = isReconnect
                ? new ContactorMode(ContactorMode.Mode.RECONNECT)
                : new ContactorMode(ContactorMode.Mode.DISCONNECT);
        ResponseFrame response = getConnection().sendFrameGetResponse(
                getContactorRequest(getDeviceIdOfMaster(), mode, serialNumber)
        );

        if (response.getData() instanceof ContactorResponseStructure) {
            return (ContactorResponseStructure) response.getData();
        } else {
            throw CommunicationException.unexpectedResponse(
                    new UnexpectedResponseException("Expected ContactorResponseStructure but was " + response.getData().getClass().getSimpleName())
            );
        }
    }


    protected RequestFrame getContactorRequest(int destinationAddress, ContactorMode mode, String serialNumber) {
        RequestFrame request = new RequestFrame();
        request.setDestinationAddress(new Address(destinationAddress));
        request.setFunction(new Function(FunctionCode.CONTACTOR_REQUEST));
        ContactorRequestStructure contactorRequestStructure = new ContactorRequestStructure(getTimeZone());
        contactorRequestStructure.setMode(mode);
        contactorRequestStructure.setSerialNumber(serialNumber);
        request.setData(contactorRequestStructure);
        request.generateAndSetCRC();
        return request;
    }

    /**
     * Try to load the data corresponding to the given response (specified by the given string, which most likely contains the name of FunctionCode) from the cache.
     * In case no data is available, null is returned.
     */
    private Data loadResponseDataFromCache(String responseDataKey) {
        return cachedData.get(responseDataKey);
    }

    public Connection getConnection() {
        if (this.connection == null) {
            this.connection = new GPRSConnection(getComChannel(), getProperties());
        }
        return connection;
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    public void setComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    public GarnetProperties getProperties() {
        if (this.properties == null) {
            this.properties = new GarnetProperties(this.propertySpecService);
        }
        return this.properties;
    }

    public void setProperties(GarnetProperties properties) {
        this.properties = properties;
    }

    public TimeZone getTimeZone() {
        return getProperties().getTimeZone();
    }

    public int getDeviceIdOfMaster() {
        return getProperties().getDeviceId();
    }
}