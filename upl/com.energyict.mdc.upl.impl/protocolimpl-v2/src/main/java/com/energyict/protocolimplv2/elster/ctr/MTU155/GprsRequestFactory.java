package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.protocol.ComChannel;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.EK155.EK155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.SecureGprsConnection;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRNackException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.GPRSFrame;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Address;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Channel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Cpa;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.EncryptionStatus;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Function;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.StructureCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.AckStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayEventsQueryRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayEventsQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayQueryRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.DownloadRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.EndOfSessionRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ExecuteRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.InitialisationCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.NackStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.RegisterQueryRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.RegisterQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.RegisterWriteRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECFQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableQueryRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TraceQueryRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TraceQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.AckAdditionalDownloadData;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.CIA;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Counter_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Group;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Identify;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Index_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NumberOfElements;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace_C;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Segment;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.StartDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.VF;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.GasQuality;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.MeterInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 12-okt-2010
 * Time: 11:04:01
 */

/**
 * Remark on exception handling:
 * GprsConnection & SecureGprsConnection do not throw blocking exceptions
 * They throw CommunicationException.numberOfRetriesReached and CommunicationException.cipheringException instead.
 * Be sure to check if these exceptions are handled in the desired way.
 * If necessary add an extra catch clause for these.
 */


public class GprsRequestFactory implements RequestFactory {

    private MeterInfo meterInfo;
    private GasQuality gasQuality;
    private final GprsConnection connection;
    private MTU155Properties properties;
    private Logger logger;
    private TimeZone timeZone;
    private int writeDataBlockID;
    private IdentificationResponseStructure identificationStructure = null;

    private static final int REF_DATE_DAYS_AHEAD = 0;
    public static final int LENGTH_CODE_PER_REQUEST_LONG_FRAMES = 1000;
    public static final int LENGTH_CODE_PER_REQUEST_SHORT_FRAMES = 116;
    private List<AbstractCTRObject> cachedObjects;
    protected TableDECFQueryResponseStructure tableDECF;
    protected TableDECQueryResponseStructure tableDEC;
    protected boolean isEK155Protocol;

    /**
     * @param comChannel
     * @param logger
     * @param properties
     */
    public GprsRequestFactory(ComChannel comChannel, Logger logger, MTU155Properties properties, TimeZone timeZone, boolean isEK155Protocol) {
        this(comChannel, logger, properties, timeZone, null, isEK155Protocol);
    }

    /**
     * @param comChannel
     * @param logger
     * @param properties
     * @param timeZone
     * @param identificationStructure
     */
    public GprsRequestFactory(ComChannel comChannel, Logger logger, MTU155Properties properties, TimeZone timeZone, IdentificationResponseStructure identificationStructure, boolean isEK155Protocol) {
        this.connection = new SecureGprsConnection(comChannel, properties, logger);
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
        this.identificationStructure = identificationStructure;
        this.cachedObjects = new ArrayList<>();
        this.isEK155Protocol = isEK155Protocol;
    }

    public GprsConnection getConnection() {
        return connection;
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    private byte[] getPassword() {
        return getProperties().getPassword().getBytes();
    }

    public MTU155Properties getProperties() {
        if (properties == null) {
            this.properties = new MTU155Properties(TypedProperties.empty());
        }
        return properties;
    }

    public Address getAddress() {
        return new Address(getProperties().getAddress());
    }

    /**
     * Reads a meter for identification
     *
     * @return the meter's response
     * @throws CTRException
     */
    private IdentificationResponseStructure readIdentificationStructure() throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("readIdentificationStructure"));

        GPRSFrame response = getConnection().sendFrameGetResponse(getIdentificationRequest());
        if (response.getData() instanceof IdentificationResponseStructure) {
            return (IdentificationResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected IdentificationResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
    }

    /**
     * Creates a identification request for a meter
     *
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
     *
     * @param attributeType: sets the fields of the objects in the meter response
     * @param objectId:      the id's of the objects the meter should sent in it's response
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
        request.setChannel(new Channel(1));
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.REGISTER);
        request.setData(new RegisterQueryRequestStructure(false).parse(registerRequest, 0));
        return request;
    }

    /**
     * Create a trace request structure
     *
     * @param objectId:         the id of the requested object
     * @param period:           the interval period (e.g. 15 minutes)
     * @param startDate:        the start date
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
     *
     * @param objectId:      the id of the requested object
     * @param period:        the interval period (e.g. 15 minutes)
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

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException {
        CTRObjectID[] objectIds = new CTRObjectID[objectId.length];
        for (int i = 0; i < objectId.length; i++) {
            objectIds[i] = new CTRObjectID(objectId[i]);
        }
        return queryRegisters(attributeType, objectIds);
    }

    /**
     * Creates a request for a number of event records
     *
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
     * Creates a 'query against array' request for a number of records
     *
     * @param index_Q:   index of the first element to retrieve
     * @param counter_q: number of elements to retrieve
     * @return a request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getArrayRequest(CTRObjectID objectID, Index_Q index_Q, Counter_Q counter_q) throws CTRParsingException {
        byte[] pssw = getPassword();

        byte[] eventRequest = ProtocolTools.concatByteArrays(
                pssw,
                objectID.getBytes(),
                index_Q.getBytes(),
                counter_q.getBytes()
        );

        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.QUERY);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.ARRAY);
        request.setData(new ArrayQueryRequestStructure(false).parse(eventRequest, 0));
        return request;
    }

    /**
     * Creates a request to write registers in the meter.
     *
     * @param validityDate:  the validity date for the writing
     * @param wdb:           a unique number
     * @param p_Session:     the session configuration
     * @param attributeType: the fields of the object that needs to be written
     * @param objects:       the objects that need to be written
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
     * Creates a request to write registers in the meter.
     *
     * @param validityDate:  the validity date for the writing
     * @param wdb:           a unique number
     * @param p_Session:     the session configuration
     * @param attributeType: the fields of the object that needs to be written
     * @param rawData:       the rawData with the objects
     * @return a request structure
     * @throws CTRParsingException
     */
    public GPRSFrame getRegisterWriteRequest(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRParsingException {
        byte[] pssw = getPassword();
        byte[] nrObjects = new byte[]{(byte) numberOfObjects};
        byte[] writeRequest = padData(128, ProtocolTools.concatByteArrays(
                pssw,
                validityDate.getBytes(),
                wdb.getBytes(),
                p_Session.getBytes(),
                nrObjects,
                attributeType.getBytes(),
                rawData
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
     *
     * @param validityDate: the validity date
     * @param wdb:          a unique number
     * @param id:           the id of the object that needs to be written
     * @param data:         the data one wants to write
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
     *
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
     *
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

    public AbstractCTRObject queryRegister(String objectID) throws CTRException {
        AbstractCTRObject objectFromCache = getObjectFromCache(objectID);
        if (objectFromCache != null) {
            return objectFromCache;
        }

        List<AbstractCTRObject> objects = queryRegisters(new AttributeType(0x03), objectID);
        if ((objects != null) && (!objects.isEmpty())) {
            return objects.get(0);
        } else {
            return null;
        }
    }

    public List<AbstractCTRObject> queryRegisters(CTRObjectID... objectId) throws CTRException {
        return queryRegisters(new AttributeType(0x03), objectId);
    }

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, CTRObjectID... objectId) throws CTRException {
        return queryRegisters(attributeType, false, objectId);
    }

    private List<AbstractCTRObject> queryRegisters(AttributeType attributeType, boolean recursive, CTRObjectID... objectId) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryRegisters", objectId));

        //Send the request with IDs, get the response containing objects
        GPRSFrame response = getConnection().sendFrameGetResponse(getRegisterRequest(attributeType, objectId));
        response.doParse();

        //Parse the response into a list of objects
        RegisterQueryResponseStructure registerResponse;
        if (response.getData() instanceof RegisterQueryResponseStructure) {
            registerResponse = (RegisterQueryResponseStructure) response.getData();
            List<AbstractCTRObject> objects = new ArrayList<>();
            objects.addAll(Arrays.asList(registerResponse.getObjects()));
            CTRObjectID[] remainingObjectIDs = validateRegisterResponse(objects, objectId);
            if (!recursive && remainingObjectIDs.length > 0) {
                List<AbstractCTRObject> abstractCTRObjects = queryRegisters(attributeType, true, remainingObjectIDs);
                objects.addAll(abstractCTRObjects);
            }

            cachedObjects.addAll(objects);
            return objects;

        } else if (response.getData() instanceof NackStructure) {
            NackStructure nackStructure = (NackStructure) response.getData();
            if (nackStructure.getReason().getReason() == 0x45) {    // Response to the Query (Overflow): More data items have been requested than the permitted number
                List<AbstractCTRObject> objects = new ArrayList<>();
                // Split the query in 2 smaller queries and try again.
                objects.addAll(queryRegisters(attributeType, false, getSubArray(objectId, 0, Math.abs(objectId.length / 2))));
                objects.addAll(queryRegisters(attributeType, false, getSubArray(objectId, Math.abs(objectId.length / 2), objectId.length)));
                return objects;
            } else {
                throw new CTRException("Expected RegisterResponseStructure but was " + response.getData().getClass().getSimpleName());
            }
        } else {
            throw new CTRException("Expected RegisterResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
    }

    private CTRObjectID[] validateRegisterResponse(List<AbstractCTRObject> objects, CTRObjectID[] objectId) throws CTRException {
        List<CTRObjectID> remainingObjectIdList = new ArrayList<>();
        for (int i = 0; i < objectId.length; i++) {
            CTRObjectID requestedId = objectId[i];
            if (objects.size() > i) {
                AbstractCTRObject receivedObject = objects.get(i);
                CTRObjectID receivedId = receivedObject.getId();
                if ((receivedId != null) && (requestedId != null)) {
                    if (!receivedId.toString().equalsIgnoreCase(requestedId.toString())) {
                        throw new CTRException("Expected [" + requestedId.toString() + "] but received [" + receivedId.toString() + "] while reading registers.");
                    }
                }
            } else {
                remainingObjectIdList.add(objectId[i]);
            }
        }
        CTRObjectID[] remainingObjectIds = new CTRObjectID[remainingObjectIdList.size()];
        return remainingObjectIdList.toArray(remainingObjectIds);
    }

    /**
     * retrieve the subArray [from, to[  out of the given array
     *
     * @param array
     * @param from  Inclusive from
     * @param to    Exclusive to
     * @return
     */
    public static CTRObjectID[] getSubArray(final CTRObjectID[] array, final int from, final int to) {
        CTRObjectID[] subArray;
        if (isArrayIndexInRange(array, from) && isArrayIndexInRange(array, to - 1) && (from < to)) {
            subArray = new CTRObjectID[to - from];
            for (int i = 0; i < subArray.length; i++) {
                subArray[i] = array[i + from];
            }
        } else {
            subArray = new CTRObjectID[0];
        }
        return subArray;
    }

    public static boolean isArrayIndexInRange(final CTRObjectID[] array, final int index) {
        return (array != null) && (index >= 0) && (array.length > index);
    }

    /**
     * ***** SECTION 'FIRMWARE UPGRADING' ****** *
     */
    public void doInitFirmwareUpgrade(Identify newSoftwareIdentifier, CIA cia, VF vf, Calendar activationDate, int size, boolean useLongFrameFormat) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("doInitFirmwareUpgrade"));

        // 1. Abort previous download progress and re-initializes all download parameters.
        //Not needed - in some cases this doesn't work -- executing Step 2 contains an implicit re-init.

        // 2. Send initialization command with new identify and all init parameters in formatted code field.
        GPRSFrame response = getConnection().sendFrameGetResponse(getInitDownloadParametersRequest(newSoftwareIdentifier, cia, vf, activationDate, size, useLongFrameFormat));
        response.doParse();

        //Check if the response is an ACK
        if (response.getData() instanceof AckStructure) {
            AckAdditionalDownloadData downloadData = new AckAdditionalDownloadData().parse(((AckStructure) response.getData()).getAdditionalData().getBytes(), 0);
            if (!downloadData.getIdentify().equals(newSoftwareIdentifier)) {
                throw new CTRException("Could not initialize the firmware upgrade parameters.");
            }
        } else {
            // MTU155 responded with NACK
            throw new CTRException("Expected AckStructure but was " + response.getData().getClass().getSimpleName() + " - failed to initialize the Firmware Upgrade.");
        }
    }

    public boolean doSendFirmwareSegment(Identify newSoftwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment, boolean useLongFrameFormat) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("doSendFirmwareSegment") + " - Segment: " + (lastAckedSegment.getSegment() + 1));
        GPRSFrame response = getConnection().sendFrameGetResponse(getFirmwareCodeTransferRequest(newSoftwareIdentifier, firmwareUpgradeFile, lastAckedSegment, useLongFrameFormat));
        response.doParse();

        //Check if the response is an ACK
        Data downloadResponse;
        if (response.getData() instanceof AckStructure) {
            downloadResponse = response.getData();
            AckAdditionalDownloadData ackAdditionalDownloadData = new AckAdditionalDownloadData().parse(((AckStructure) downloadResponse).getAdditionalData().getBytes(), 0);

            // If The AckAdditionalDownloadData contains the segment number, we have guarantee the segment (and all previous ones) is correct received.
            int segmentSend = lastAckedSegment.getSegment() + 1;

            if (ackAdditionalDownloadData.getSegment().getSegment() != segmentSend) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /** ****** SECTION 'REQUEST MESSAGES SPECIFIC FOR FIRMWARE UPGRADING' ****** **/
    /**
     * FASE 1 [initialization step] of firmware upgrade process. This request will initialize the firmware upgrade procces.
     * This request will set the download parameters object in the MTU1555 corresponding to the information of the new firmware.
     * In FASE 2 [code transfer step] the actual firmware code will be sent.
     */
    private GPRSFrame getInitDownloadParametersRequest(Identify newSoftwareIdentifier, CIA cia, VF vf, Calendar activationDate, int size, boolean useLongFrameFormat) throws CTRException {
        newSoftwareIdentifier.setIdentify(newSoftwareIdentifier.getIdentify());
        Group group_s, group_c;
        if (isEK155Protocol) {
            group_s = new Group(0);
            group_c = new Group(0);
        } else {
            group_s = new Group(25);      // Not meaningful for MTU155
            group_c = new Group(1);       // Different from 0

            // Construct special "Initialization code" field with all information of the new firmware
            List<AbstractCTRObject> objects = getObjects("9.0.2");
            if ((objects == null) || (objects.size() <= 0)) {
                throw new CTRException("Unable to read the equipment identification code! List of objects returned was empty or null.");
            }
            AbstractCTRObject info = objects.get(0);    // Equipment Identification Code object
            String CIA = info.getValue(0).getStringValue();     // CIA must be the Equipment Identification code
            cia = new CIA(CIA);

            String VF = newSoftwareIdentifier.getHexIdentify();
            while (VF.length() < 6) {
                VF = "0" + VF;
            }
            vf = new VF(VF);

        }
        Segment segment = new Segment(0);   //This will be the first segment
        InitialisationCode initialisationCode = new InitialisationCode(activationDate, cia, vf, size, useLongFrameFormat);

        byte[] downloadRequest = ProtocolTools.concatByteArrays(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD).getBytes(),
                WriteDataBlock.getRandomWDB().getBytes(),
                newSoftwareIdentifier.getBytes(),
                group_s.getBytes(),
                group_c.getBytes(),
                segment.getBytes(),
                initialisationCode.getBytes()
        );
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getProfi().setLongFrame(false);

        request.getFunctionCode().setFunction(Function.DOWNLOAD);
        request.getStructureCode().setStructureCode(0);
        request.setChannel(new Channel(0));
        request.setData(new DownloadRequestStructure(false).parse(downloadRequest, 0));
        return request;
    }

    /**
     * FASE 2 [code transfer step] request, which is used to send the actual code segments to the MTU155.
     */
    private GPRSFrame getFirmwareCodeTransferRequest(Identify softwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment, boolean useLongFrameFormat) throws CTRParsingException {
        Group group_s, group_c;
        int lengthCodePerRequest = useLongFrameFormat ? LENGTH_CODE_PER_REQUEST_LONG_FRAMES : LENGTH_CODE_PER_REQUEST_SHORT_FRAMES;

        if (isEK155Protocol) {
            group_s = new Group(1);
            group_c = new Group(1);
        } else {
            group_s = new Group(25);  // Not used for MTU155 - set to 25
            group_c = new Group(1);   // Not used for MTU155 - set to 1
        }

        byte[] code = new byte[lengthCodePerRequest];
        int i = firmwareUpgradeFile.length - lastAckedSegment.getSegment() * lengthCodePerRequest;
        System.arraycopy(firmwareUpgradeFile, lastAckedSegment.getSegment() * lengthCodePerRequest, code, 0, (i > lengthCodePerRequest) ? lengthCodePerRequest : i);
        // E.g.: lastAckedSegment = 0 -> code will contain image bytes 0 to 999 (when using long frame format).
        //       lastAckedSegment = 1 -> code will contain image bytes 1000 -> 1999.

        Segment newSegmentNumber = new Segment(lastAckedSegment.getSegment() + 1);   // Segment numbers are 1 based - 0 is reserved for special initialization segment.

        byte[] downloadRequest = ProtocolTools.concatByteArrays(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD).getBytes(),
                WriteDataBlock.getRandomWDB().getBytes(),
                softwareIdentifier.getBytes(),
                group_s.getBytes(),
                group_c.getBytes(),
                newSegmentNumber.getBytes(),
                code
        );
        GPRSFrame request = new GPRSFrame(useLongFrameFormat);
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getProfi().setLongFrame(useLongFrameFormat);

        request.getFunctionCode().setFunction(Function.DOWNLOAD);
        request.getStructureCode().setStructureCode(0);
        request.setChannel(new Channel(0));
        request.setData(new DownloadRequestStructure(useLongFrameFormat).parse(downloadRequest, 0));
        return request;
    }
    /** ****** END OF SECTION 'REQUEST MESSAGES SPECIFIC FOR FIRMWARE UPGRADING' ****** **/
    /**
     * ***** END OF SECTION 'FIRMWARE UPGRADING' ****** *
     */

    public TableDECFQueryResponseStructure queryTableDECF() throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryTableDECF"));
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

    public TableDECQueryResponseStructure queryTableDEC() throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryTableDEC"));
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

    public Data executeRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("executeRequest", id));
        GPRSFrame response = getConnection().sendFrameGetResponse(getExecuteRequest(validityDate, wdb, id, data));
        response.doParse();

        //Check the response: should be Ack or Nack
        Data executeResponse;
        if (response.getData() instanceof AckStructure) {
            executeResponse = response.getData();
        } else if (response.getData() instanceof NackStructure) {
            throw new CTRNackException((NackStructure) response.getData());
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return executeResponse;
    }

    public Data executeRequest(CTRObjectID id, byte[] data) throws CTRException {
        return executeRequest(ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD), WriteDataBlock.getRandomWDB(), id, data);
    }

    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("writeRegister", objects));
        GPRSFrame response = getConnection().sendFrameGetResponse(getRegisterWriteRequest(validityDate, wdb, p_Session, attributeType, objects));

        //Check the response: should be Ack or Nack
        Data writeRegisterResponse;
        response.doParse();
        if (response.getData() instanceof AckStructure) {
            writeRegisterResponse = response.getData();
        } else if (response.getData() instanceof NackStructure) {
            throw new CTRNackException((NackStructure) response.getData());
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return writeRegisterResponse;
    }

    public Data writeRegister(AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("writeRegister", "A.3.6", "A.4.6", "A.5.6", "A.C.3", "A.C.4", "A.C.0", "A.C.8"));
        GPRSFrame registerWriteRequest = getRegisterWriteRequest(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD),
                WriteDataBlock.getRandomWDB(),
                P_Session.getOpenAndClosePSession(),
                attributeType,
                numberOfObjects,
                rawData
        );
        GPRSFrame response = getConnection().sendFrameGetResponse(registerWriteRequest);

        //Check the response: should be Ack or Nack
        Data writeRegisterResponse;
        response.doParse();
        if (response.getData() instanceof AckStructure) {
            writeRegisterResponse = response.getData();
        } else if (response.getData() instanceof NackStructure) {
            throw new CTRNackException((NackStructure) response.getData());
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return writeRegisterResponse;
    }

    public Data writeRegister(AbstractCTRObject... objects) throws CTRException {
        return writeRegister(ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD), WriteDataBlock.getRandomWDB(), P_Session.getOpenAndClosePSession(), AttributeType.getValueAndObjectId(), objects);
    }

    public List<AbstractCTRObject> queryTrace(CTRObjectID id, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryTrace", id));

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

    public ArrayEventsQueryResponseStructure queryEventArray(int index_Q) throws CTRException {
        return queryEventArray(new Index_Q(index_Q));
    }

    public ArrayEventsQueryResponseStructure queryEventArray(Index_Q index_Q) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryEventArray") + " - Index_Q: " + index_Q);

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

    public ArrayQueryResponseStructure queryArray(CTRObjectID objectID, Index_Q index_q, Counter_Q counter_q) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryArray", objectID) + " - Index_Q: " + index_q);

        GPRSFrame response = getConnection().sendFrameGetResponse(getArrayRequest(objectID, index_q, counter_q));

        //Parse the records in the response into objects.
        response.doParse();
        ArrayQueryResponseStructure arrayResponse;

        if (response.getData() instanceof ArrayQueryResponseStructure) {
            arrayResponse = (ArrayQueryResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected ArrayEventsResponseStructure but was " + response.getData().getClass().getSimpleName());
        }

        return arrayResponse;
    }

    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRException {
        logObjectIDInfo(getDebugObjectIDsInfo("queryTrace_C", id) + " - Reference date: " + referenceDate.getCalendar(getTimeZone()).getTime());

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
     *
     * @param length:    the given length
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

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public WriteDataBlock getNewWriteDataBlock() {
        WriteDataBlock randomWDB = WriteDataBlock.getRandomWDB();
        writeDataBlockID = randomWDB.getWdb();
        return randomWDB;
    }

    public int getWriteDataBlockID() {
        return writeDataBlockID;
    }

    public void sendEndOfSession() {
        logObjectIDInfo(getDebugObjectIDsInfo("sendEndOfSession"));

        getLogger().severe("Closing session. Sending End Of Session Request.");
        try {
            if (getConnection() != null) {
                getConnection().sendFrameGetResponse(getEndOfSessionRequest());
            } else {
                getLogger().warning("Unable to close connection. getConnection() returned null.");
            }
        } catch (ProtocolRuntimeException e) {
            getLogger().severe("Failed to close session! " + e.getMessage());
            throw ConnectionCommunicationException.protocolDisconnectFailed(e);
        }
    }

    /**
     * Get a new EndOf session request
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

    public String getIPAddress() {
        getLogger().severe("SmsRequestFactory - getIPAddress method is not supported.");
        return "Unknown";
        //ToDo: getLink() will always return null!
//        String ipAddress = null;
//        if ((getLink() != null) && (getLink().getStreamConnection() != null) && (getLink().getStreamConnection().getSocket() != null)) {
//            InetAddress address = getLink().getStreamConnection().getSocket().getInetAddress();
//            if (address != null) {
//                ipAddress = address.getHostAddress();
//            }
//        }
//        return ipAddress == null ? "Unknown" : ipAddress;
    }

    public MeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new MeterInfo(this, getLogger(), getTimeZone());
        }
        return meterInfo;
    }

    public void setMeterInfo(MeterInfo meterInfo) {
        this.meterInfo = meterInfo;
    }

    public boolean isEK155Protocol() {
        return isEK155Protocol;
    }

    public List<AbstractCTRObject> getObjects(String... objectIDs) {
        CTRObjectID[] objectIds = new CTRObjectID[objectIDs.length];
        for (int i = 0; i < objectIDs.length; i++) {
            objectIds[i] = new CTRObjectID(objectIDs[i]);
        }

        return getObjects(objectIds);
    }

    public List<AbstractCTRObject> getObjects(CTRObjectID... objectIDs) {
        List<AbstractCTRObject> objects = new ArrayList<>();

        // List of objects who need to be queried for in a query for register structure.
        List<CTRObjectID> objectIDsToQuery = new ArrayList<>();

        for (int i = 0; i < objectIDs.length; i++) {
            AbstractCTRObject object = null;
            try {
                if (object == null) {
                    object = getObjectFromCache(objectIDs[i]);
                }
                if (object == null) {
                    object = getObjectFromIdentificationTable(objectIDs[i]);
                }
                if (object == null) {
                    object = getObjectFromDECFTable(objectIDs[i]);
                }
                if (object == null) {
                    object = getObjectFromDECTable(objectIDs[i]);
                }
                if (object == null) {
                    addObjectIdToListOfIDsToQuery(objectIDsToQuery, objectIDs[i]);
                } else {
                    objects.add(object);
                    cachedObjects.add(object);
                }
            } catch (CTRException e) {
                getLogger().log(Level.WARNING, "Encountered CTRException while reading object " + objectIDs[i].toString() + ": " + e.getMessage());
            }
        }

        if (!objectIDsToQuery.isEmpty()) { // We have to query specific for these registers
            try {
                for (int y = 0; y < objectIDsToQuery.size(); y += 10) {  // Do not request more than 10 objects each time.
                    CTRObjectID[] objectsToQuery = new CTRObjectID[0];
                    int toIndex = objectIDsToQuery.size() < (y + 10) ? objectIDsToQuery.size() : (y + 10);
                    List<AbstractCTRObject> objectsQueried = queryRegisters(objectIDsToQuery.subList(y, toIndex).toArray(objectsToQuery));
                    objects.addAll(objectsQueried);
                }
            } catch (CTRException e) {
                String objectIdsString = "";
                for (int i = 0; i < objectIDsToQuery.size(); i++) {
                    objectIdsString += objectIDsToQuery.get(i).toString() + ", ";
                }
                getLogger().log(Level.WARNING, "Encountered CTRException while reading objects " + objectIdsString + ": " + e.getMessage());
            }
        }
        return objects;
    }

    private void addObjectIdToListOfIDsToQuery(List<CTRObjectID> objectIDsToQuery, CTRObjectID objectID) {
        for (CTRObjectID each : objectIDsToQuery) {
            if (each.toString().equals(objectID.toString()))  {
                return; // The ObjectId is already in the list, so no need to add it again
            }
        }

        objectIDsToQuery.add(objectID);
    }

    protected AbstractCTRObject getObjectFromCache(String idObject) {
        return getObjectFromCache(new CTRObjectID(idObject));
    }

    protected AbstractCTRObject getObjectFromCache(CTRObjectID idObject) {
        for (AbstractCTRObject each : cachedObjects) {
            if (each.getId().toString().equals(idObject.toString())) {
                return each;
            }
        }
        return null;
    }

    /**
     * Check if the requested object is in the Identification table, and read it if it is
     *
     * @param objectId: the id of the requested CTR Object
     * @return the matching CTR Object, if it is in the Identification table.
     * @throws CTRException
     */
    protected AbstractCTRObject getObjectFromIdentificationTable(CTRObjectID objectId) throws CTRException {
        if (IdentificationResponseStructure.containsObjectId(objectId)) {
            for (AbstractCTRObject ctrObject : getIdentificationStructure().getObjects()) {
                if (ctrObject.getId().toString().equals(objectId.toString())) {
                    return ctrObject;
                }
            }
        }
        return null;
    }

    /**
     * Check if the requested object is in the DECF table, and read it if it is
     *
     * @param objectId: the id of the requested CTR Object
     * @return the matching CTR Object, if it is in the decf table
     * @throws CTRException
     */
    protected AbstractCTRObject getObjectFromDECFTable(CTRObjectID objectId) throws CTRException {
        if (TableDECFQueryResponseStructure.containsObjectId(objectId)) {
            for (AbstractCTRObject ctrObject : getTableDECF().getObjects()) {
                if (ctrObject.getId().toString().equals(objectId.toString())) {
                    return ctrObject;
                }
            }
        }
        return null;
    }

    /**
     * Check if the requested object is in the DEC table, and read it if it is
     *
     * @param objectId: the id of the requested CTR Object
     * @return the matching CTR Object, if it is in the dec table
     * @throws CTRException
     */
    protected AbstractCTRObject getObjectFromDECTable(CTRObjectID objectId) throws CTRException {
        if (TableDECQueryResponseStructure.containsObjectId(objectId)) {
            for (AbstractCTRObject ctrObject : getTableDEC().getObjects()) {
                if (ctrObject.getId().toString().equals(objectId.toString())) {
                    return ctrObject;
                }
            }
        }
        return null;
    }

    public TableDECFQueryResponseStructure getTableDECF() throws CTRException {
        if (tableDECF == null) {
            tableDECF = queryTableDECF();
        }
        return tableDECF;
    }

    public TableDECQueryResponseStructure getTableDEC() throws CTRException {
        if (tableDEC == null) {
            tableDEC = queryTableDEC();
        }
        return tableDEC;
    }

    public GasQuality getGasQuality() {
        if (gasQuality == null) {
            gasQuality = new GasQuality(this, getLogger());
        }
        return gasQuality;
    }

    public String getDebugObjectIDsInfo(String method) {
        return "Object access in method " + method;
    }

    public String getDebugObjectIDsInfo(String method, String... objectIDs) {
        String msg = "Object access in method " + method + " for IDs ";
        msg += "[";
        msg += objectIDs[0];
        for (int i = 1; i < objectIDs.length; i++) {
            msg += ", " + objectIDs[i];
        }
        msg += "]";
        return msg;
    }

    public String getDebugObjectIDsInfo(String method, CTRObjectID... objectIDs) {
        String msg = "Object access in method " + method + " for " + (objectIDs.length == 1 ? "ID" : "IDs ");
        msg += "[";
        msg += objectIDs[0];
        for (int i = 1; i < objectIDs.length; i++) {
            msg += ", " + objectIDs[i];
        }
        msg += "]";
        return msg;
    }

    public String getDebugObjectIDsInfo(String method, AbstractCTRObject... objects) {
        String msg = "Object access in method " + method + " for IDs ";
        msg += "[";
        msg += objects[0].getId();
        for (int i = 1; i < objects.length; i++) {
            msg += ", " + objects[i].getId();
        }
        msg += "]";
        return msg;
    }

    public void logObjectIDInfo(String msg) {
        if (isEK155Protocol && ((EK155Properties) getProperties()).isLogObjectIDs()) {
            System.out.println(msg);
            getLogger().log(Level.FINEST, msg);
        }
    }
}
