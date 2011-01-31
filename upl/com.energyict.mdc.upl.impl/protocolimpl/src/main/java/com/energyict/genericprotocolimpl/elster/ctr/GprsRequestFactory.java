package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureGprsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.*;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 12-okt-2010
 * Time: 11:04:01
 */
public class GprsRequestFactory {

    private final GprsConnection connection;
    private MTU155Properties properties;
    private Logger logger;
    private TimeZone timeZone;
    private IdentificationResponseStructure identificationStructure = null;

    /**
     * @param link
     * @param logger
     * @param properties
     */
    public GprsRequestFactory(Link link, Logger logger, MTU155Properties properties, TimeZone timeZone) {
        this(link.getInputStream(), link.getOutputStream(), logger, properties, timeZone);
    }

    /**
     * @param inputStream
     * @param outputStream
     * @param logger
     * @param properties
     */
    public GprsRequestFactory(InputStream inputStream, OutputStream outputStream, Logger logger, MTU155Properties properties, TimeZone timeZone) {
        this(inputStream, outputStream, logger, properties, timeZone, null);
    }

    /**
     *
     * @param link
     * @param logger
     * @param properties
     * @param timeZone
     * @param identificationStructure
     */
    public GprsRequestFactory(Link link, Logger logger, MTU155Properties properties, TimeZone timeZone, IdentificationResponseStructure identificationStructure) {
        this(link.getInputStream(), link.getOutputStream(), logger, properties, timeZone, identificationStructure);
    }

    /**
     * 
     * @param inputStream
     * @param outputStream
     * @param logger
     * @param properties
     * @param timeZone
     * @param identificationStructure
     */
    public GprsRequestFactory(InputStream inputStream, OutputStream outputStream, Logger logger, MTU155Properties properties, TimeZone timeZone, IdentificationResponseStructure identificationStructure) {
        this.connection = new SecureGprsConnection(inputStream, outputStream, properties, logger);
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
        this.identificationStructure = identificationStructure;
    }

    /**
     * Getter for the connection
     *
     * @return
     */
    public GprsConnection getConnection() {
        return connection;
    }

    /**
     * Getter for the logger object.
     * If there is no logger, create a new one
     *
     * @return
     */
    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    private byte[] getPassword() {
        return getProperties().getPassword().getBytes();
    }


    /**
     * Getter for the protocol properties
     *
     * @return
     */
    public MTU155Properties getProperties() {
        if (properties == null) {
            this.properties = new MTU155Properties();
        }
        return properties;
    }

    /**
     * Create a new address, with a value from the protocolProperties
     *
     * @return
     */
    public Address getAddress() {
        return new Address(getProperties().getAddress());
    }

    /**
     * Reads a meter for identification 
     * @return the meter's response
     * @throws CTRException
     */
    private IdentificationResponseStructure readIdentificationStructure() throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getIdentificationRequest());
        if (response.getData() instanceof IdentificationResponseStructure) {
            return (IdentificationResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected IdentificationResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
    }

    /**
     * Creates a identification request for a meter
     * @return the identification request
     */
    public GPRSFrame getIdentificationRequest() {
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getProfi().setLongFrame(false);
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.IDENTIFICATION_REQUEST);
        request.getStructureCode().setStructureCode(StructureCode.IDENTIFICATION);
        request.setData(new IdentificationRequestStructure());
        request.setCpa(new Cpa(0x00));
        return request;
    }

    /**
     * Creates a register request structure
     * @param attributeType: sets the fields of the objects in the meter response
     * @param objectId: the id's of the objects the meter should sent in it's response
     * @return the request
     * @throws CTRParsingException
     */
    public GPRSFrame getRegisterRequest(AttributeType attributeType, CTRObjectID[] objectId) throws CTRParsingException {
        byte[] pssw = getPassword();
        byte[] numberOfObjects = new byte[]{(byte) objectId.length};
        byte[] type = attributeType.getBytes();
        byte[] ids = new byte[0];
        for (CTRObjectID id : objectId) {
            ids = ProtocolTools.concatByteArrays(ids, id.getBytes());
        }

        byte[] registerRequest = ProtocolTools.concatByteArrays(
                pssw,
                numberOfObjects,
                type,
                ids
        );

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.setChannel(new Channel(1));   //TODO
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.REGISTER);
        request.setData(new RegisterQueryRequestStructure(false).parse(registerRequest, 0));
        return request;
    }

    /**
     * Create a trace request structure
     * @param objectId: the id of the requested object
     * @param period: the interval period (e.g. 15 minutes)
     * @param startDate: the start date
     * @param numberOfElements: the number of interval records that should be returned 
     * @return a request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getTraceRequest(CTRObjectID objectId, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRParsingException {
        byte[] pssw = getPassword();
        byte[] traceRequest = ProtocolTools.concatByteArrays(
                pssw,
                objectId.getBytes(),
                period.getBytes(),
                startDate.getBytes(),
                numberOfElements.getBytes()
        );

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.TRACE);
        request.setData(new TraceQueryRequestStructure(request.getProfi().isLongFrame()).parse(traceRequest, 0));
        return request;
    }

    /**
     * Create a trace_c request structure
     * @param objectId: the id of the requested object
     * @param period: the interval period (e.g. 15 minutes)
     * @param referenceDate: the reference date
     * @return a request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getTrace_CRequest(CTRObjectID objectId, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRParsingException {
        byte[] pssw = getProperties().getPassword().getBytes();
        byte[] trace_CRequest = ProtocolTools.concatByteArrays(
                pssw,
                objectId.getBytes(),
                period.getBytes(),
                referenceDate.getBytes()
        );

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.setChannel(new Channel(1));
        request.getStructureCode().setStructureCode(StructureCode.TRACE_C);
        request.setData(new Trace_CQueryRequestStructure(request.getProfi().isLongFrame()).parse(trace_CRequest, 0));
        return request;
    }

    /**
     * Requests a number of objects from the meter, via a query.
     * @param attributeType: determines the fields of the objects in the meter's response
     * @param objectId: the id's of the requested objects
     * @return a list of objects, as requested
     * @throws CTRException
     */
    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException {
        CTRObjectID[] objectIds = new CTRObjectID[objectId.length];
        for (int i = 0; i < objectId.length; i++) {
            objectIds[i] = new CTRObjectID(objectId[i]);
        }
        return queryRegisters(attributeType, objectIds);
    }

    /**
     * Creates a request for a number of event records
     * @param index_Q: number of event records
     * @return a request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getEventArrayRequest(Index_Q index_Q) throws CTRParsingException {
        byte[] pssw = getPassword();
        byte[] eventArrayRequest = ProtocolTools.concatByteArrays(
                pssw,
                index_Q.getBytes()
        );

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.EVENT_ARRAY);
        request.setData(new ArrayEventsQueryRequestStructure(false).parse(eventArrayRequest, 0));
        return request;
    }

    /**
     * Creates a request to write registers in the meter.
     * @param validityDate: the validity date for the writing
     * @param wdb: a unique number
     * @param p_Session: the session configuration
     * @param attributeType: the fields of the object that needs to be written
     * @param objects: the objects that need to be written
     * @return a request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getRegisterWriteRequest(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRParsingException {
        byte[] pssw = getPassword();
        byte[] objectBytes = new byte[]{};
        for (AbstractCTRObject object : objects) {
            objectBytes = ProtocolTools.concatByteArrays(objectBytes, object.getBytes());
        }
        byte[] numberOfObjects = new byte[]{(byte) objects.length};
        byte[] writeRequest = padData(128, ProtocolTools.concatByteArrays(
                pssw,
                validityDate.getBytes(),
                wdb.getBytes(),
                p_Session.getBytes(),
                numberOfObjects,
                attributeType.getBytes(),
                objectBytes
        ));

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.WRITE);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.REGISTER);
        request.setData(new RegisterWriteRequestStructure(false).parse(writeRequest, 0));
        return request;
    }


    /**
     * Creates an execute request
     * @param validityDate: the validity date
     * @param wdb: a unique number
     * @param id: the id of the object that needs to be written
     * @param data: the data one wants to write
     * @return the request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getExecuteRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRParsingException {
        byte[] executeRequest = ProtocolTools.concatByteArrays(
                getPassword(),
                validityDate.getBytes(),
                wdb.getBytes(),
                id.getBytes(),
                data
        );

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.EXECUTE);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(0);
        request.setData(new ExecuteRequestStructure(false).parse(executeRequest, 0));
        return request;
    }

    /**
     * Create a decf table request
     * @return the request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getTableDECFRequest() throws CTRParsingException {
        byte[] tableRequestBytes = getPassword();
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.TABLE_DECF);
        request.setChannel(new Channel(0x01));
        request.setData(new TableQueryRequestStructure(false).parse(tableRequestBytes, 0));
        return request;
    }

    /**
     * Creates a dec table request
     * @return the request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getTableDECRequest() throws CTRParsingException {
        byte[] tableRequestBytes = getPassword();
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.TABLE_DEC);
        request.setChannel(new Channel(0x01));
        request.setData(new TableQueryRequestStructure(false).parse(tableRequestBytes, 0));
        return request;
    }

    /**
     * Returns a list of requested objects
     * @param attributeType: determines the fields of the objects
     * @param objectId: the id's of the requested objects
     * @return list of requested objects
     * @throws CTRConnectionException
     */
    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, CTRObjectID... objectId) throws CTRException {

        //Send the request with IDs, get the response containing objects
        GPRSFrame response = getConnection().sendFrameGetResponse(getRegisterRequest(attributeType, objectId));
        response.doParse();

        //Parse the response into a list of objects
        RegisterQueryResponseStructure registerResponse;
        if (response.getData() instanceof RegisterQueryResponseStructure) {
            registerResponse = (RegisterQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected RegisterResponseStructure but was " + response.getData().getClass().getSimpleName());
        }

        return Arrays.asList(registerResponse.getObjects());
    }

    /**
     * Queries for a decf table. Returns the meter response.
     * @return the meter response, being a decf table 
     * @throws CTRException, if the meter's answer was not a decf table
     */
    public TableDECFQueryResponseStructure queryTableDECF() throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getTableDECFRequest());
        response.doParse();

        TableDECFQueryResponseStructure tableDECFresponse;
        if (response.getData() instanceof TableDECFQueryResponseStructure) {
            tableDECFresponse = (TableDECFQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected TableDECFResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
        return tableDECFresponse;
    }

    /**
     * Queries for a dec table. Returns the meter response.
     * @return the meter response, being a dec table
     * @throws CTRException, if the meter's answer was not a dec table
     */
    public TableDECQueryResponseStructure queryTableDEC() throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getTableDECRequest());
        response.doParse();

        TableDECQueryResponseStructure tableDECresponse;
        if (response.getData() instanceof TableDECQueryResponseStructure) {
            tableDECresponse = (TableDECQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected TableDECResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
        return tableDECresponse;
    }

    /**
     * Executes a certain request
     * @param validityDate: the validity date
     * @param wdb: a unique number
     * @param id: the object's id
     * @param data: data for the execute request
     * @return the meter's response (ack or nack)
     * @throws CTRException, when the meter's response was unexpected.
     */
    public Data executeRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getExecuteRequest(validityDate, wdb, id, data));
        response.doParse();

        //Check the response: should be Ack or Nack
        Data executeResponse;
        if (response.getData() instanceof AckStructure) {
            executeResponse = (AckStructure) response.getData();
        } else if (response.getData() instanceof NackStructure) {
            executeResponse = (NackStructure) response.getData();
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return executeResponse;
    }

    /**
     * Writes to register(s) in the meter
     * @param validityDate: the validity date
     * @param wdb: a unique number
     * @param p_Session: the session configuration
     * @param attributeType: determines the fields of the objects
     * @param objects: the objects that should be written in the meter
     * @return: the meter's response (ack or nack)
     * @throws CTRException, if the meter's response was not recognized
     */
    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getRegisterWriteRequest(validityDate, wdb, p_Session, attributeType, objects));

        //Check the response: should be Ack or Nack
        Data writeRegisterResponse;
        response.doParse();
        if (response.getData() instanceof AckStructure) {
            writeRegisterResponse = (AckStructure) response.getData();
        } else if (response.getData() instanceof NackStructure) {
            writeRegisterResponse = (NackStructure) response.getData();
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return writeRegisterResponse;
    }

    /**
     * Do a trace query
     * @param id: the id of the object you need interval data from
     * @param period: the interval period (e.g.: 15 minutes)
     * @param startDate: the start date
     * @param numberOfElements: the number of interval values returned
     * @return: a list of objects
     * @throws CTRException, when the meter's response was not recognized
     */
    public List<AbstractCTRObject> queryTrace(CTRObjectID id, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRException {

        //Send the id, the period (15min, 1h, 1day, ...), and the start date.
        GPRSFrame response = getConnection().sendFrameGetResponse(getTraceRequest(id, period, startDate, numberOfElements));
        response.doParse();

        //Parse the records in the response into objects.
        TraceQueryResponseStructure traceResponse;
        if (response.getData() instanceof TraceQueryResponseStructure) {
            traceResponse = (TraceQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected TraceResponseStructure but was " + response.getData().getClass().getSimpleName());
        }

        return traceResponse.getTraceData();
    }

    /**
     * Queries for a number of events
     * @param index_Q: number of events to query for
     * @return the meter's response containing event records
     * @throws CTRException, if the meter's response was not recognized
     */
    public ArrayEventsQueryResponseStructure queryEventArray(Index_Q index_Q) throws CTRException {

        GPRSFrame response = getConnection().sendFrameGetResponse(getEventArrayRequest(index_Q));

        //Parse the records in the response into objects.
        response.doParse();
        ArrayEventsQueryResponseStructure arrayResponse;

        if (response.getData() instanceof ArrayEventsQueryResponseStructure) {
            arrayResponse = (ArrayEventsQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected ArrayEventsResponseStructure but was " + response.getData().getClass().getSimpleName());
        }

        return arrayResponse;
    }

    /**
     * Do a trace_C query
     * @param id: the id of the requested object
     * @param period: the interval period
     * @param referenceDate: the reference date
     * @return the meter's response
     * @throws CTRException, if the meter's response was not recognized
     */
    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRException {

        //Send the id, the period (15min, 1h, 1day, ...), and the start date.
        GPRSFrame response = getConnection().sendFrameGetResponse(getTrace_CRequest(id, period, referenceDate));
        response.doParse();

        //Parse the records in the response into objects.
        Trace_CQueryResponseStructure trace_CResponse;
        if (response.getData() instanceof Trace_CQueryResponseStructure) {
            trace_CResponse = (Trace_CQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected Trace_CQueryResponseStructure but was " + response.getData().getClass().getSimpleName());
        }

        return trace_CResponse;
    }

    /**
     * Pads a byte array to a certain length, by appending zeroes
     * @param length: the given length
     * @param fieldData: the byte array
     * @return: the padded data array
     */
    private byte[] padData(int length, byte[] fieldData) {
        int paddingLength = length - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, length);
        }
        return fieldData;
    }

    /**
     * The device timezone
     *
     * @return The device timezone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Send an end of session to the device
     */
    public void sendEndOfSession() {
        getLogger().severe("Closing session. Sending End Of Session Request.");
        try {
            getConnection().sendFrameGetResponse(getEndOfSessionRequest());
        } catch (CTRConnectionException e) {
            getLogger().severe("Failed to close session! " + e.getMessage());
        }
    }

    /**
     * Get a new EndOf session request
     *
     * @return
     */
    public GPRSFrame getEndOfSessionRequest() {
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getProfi().setLongFrame(false);
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.END_OF_SESSION);
        request.setStructureCode(new StructureCode(0x00));
        request.setChannel(new Channel(0));
        request.setData(new EndOfSessionRequestStructure());
        request.setCpa(new Cpa(0x00));
        return request;
    }

    /**
     * Getter for the cached IdentificationResponseStructure
     *
     * @return
     * @throws CTRException
     */
    public IdentificationResponseStructure getIdentificationStructure() {
        if (identificationStructure == null) {
            try {
                identificationStructure = readIdentificationStructure();
            } catch (CTRException e) {
                getLogger().severe("Unable to get the IdentificationResponseStructure: " + e.getMessage());
            }
        }
        return identificationStructure;
    }

}