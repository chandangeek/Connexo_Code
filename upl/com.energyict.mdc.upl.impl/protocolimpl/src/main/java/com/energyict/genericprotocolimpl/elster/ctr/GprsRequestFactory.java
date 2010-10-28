package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureGprsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
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

    /**
     * @param link
     * @param logger
     * @param properties
     */
    public GprsRequestFactory(Link link, Logger logger, MTU155Properties properties, TimeZone timeZone) {
        this(link.getInputStream(), link.getOutputStream(), logger, properties, timeZone);
    }

    /**
     * 
     * @param inputStream
     * @param outputStream
     * @param logger
     * @param properties
     */
    public GprsRequestFactory(InputStream inputStream, OutputStream outputStream, Logger logger, MTU155Properties properties, TimeZone timeZone) {
        this.connection = new SecureGprsConnection(inputStream, outputStream, properties);
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
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
     * @return
     * @throws CTRException
     */
    public IdentificationResponseStructure readIdentificationStructure() throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getIdentificationRequest());
        if (response.getData() instanceof IdentificationResponseStructure) {
            return (IdentificationResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected IdentificationResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
    }

    private GPRSFrame getIdentificationRequest() {
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

    private GPRSFrame getRegisterRequest(AttributeType attributeType, CTRObjectID[] objectId) throws CTRParsingException {
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

    private GPRSFrame getTraceRequest(CTRObjectID objectId, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRParsingException {
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

    private GPRSFrame getTrace_CRequest(CTRObjectID objectId, PeriodTrace period, ReferenceDate referenceDate) throws CTRParsingException {
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

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException {
        CTRObjectID[] objectIds = new CTRObjectID[objectId.length];
        for (int i = 0; i < objectId.length; i++) {
            objectIds[i] = new CTRObjectID(objectId[i]);
        }
        return queryRegisters(attributeType, objectIds);
    }

    private GPRSFrame getEventArrayRequest(Index_Q index_Q) throws CTRParsingException {
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

        private GPRSFrame getRegisterWriteRequest(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRParsingException {
        byte[] pssw = getPassword();
        byte[] objectBytes = new byte[]{};
        for (AbstractCTRObject object : objects) {
            objectBytes = ProtocolTools.concatByteArrays(objectBytes, object.getBytes());
        }
        byte[] numberOfObjects = new byte[]{(byte) objects.length};
        byte[] writeRequest = padData(125, ProtocolTools.concatByteArrays(
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


    private GPRSFrame getExecuteRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRParsingException {
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

    private GPRSFrame getTableDECFRequest() throws CTRParsingException {
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

    private GPRSFrame getTableDECRequest() throws CTRParsingException {
        byte[] tableRequestBytes = getPassword();
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.TABLE_DEC);
        request.setChannel(new Channel(0x01));
        request.setData(new TableDECQueryResponseStructure(false).parse(tableRequestBytes, 0));
        return request;
    }

    /**
     * Returns a list of requested objects
     *
     * @param attributeType
     * @param objectId
     * @return
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

    public TableDECFQueryResponseStructure queryTableDECF() throws CTRException{
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

    public TableDECQueryResponseStructure queryTableDEC() throws CTRException{
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

    public Data executeRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRException{
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

    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException{
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

    public List<AbstractCTRObject> queryTrace(CTRObjectID id, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRException {

        //Send the id, the period (15min, 1h, 1day, ...), and the start date.
        GPRSFrame response = getConnection().sendFrameGetResponse(getTraceRequest(id, period, startDate, numberOfElements ));
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

    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace period, ReferenceDate referenceDate) throws CTRException {

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


    private byte[] padData(int length, byte[] fieldData) {
        int paddingLength = length - fieldData.length;
        if (paddingLength > 0) {
            fieldData = ProtocolTools.concatByteArrays(fieldData, new byte[paddingLength]);
        } else if (paddingLength < 0) {
            fieldData = ProtocolTools.getSubArray(fieldData, 0, length);
        }
        return fieldData;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void sendEndOfSession() {
        getLogger().severe("Closing session. Sending End Of Session Request.");
        try {
            getConnection().sendFrameGetResponse(getEndOfSessionRequest());
        } catch (CTRConnectionException e) {
            getLogger().severe("Failed to close session! " + e.getMessage());
        }
    }

    private GPRSFrame getEndOfSessionRequest() {
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
}