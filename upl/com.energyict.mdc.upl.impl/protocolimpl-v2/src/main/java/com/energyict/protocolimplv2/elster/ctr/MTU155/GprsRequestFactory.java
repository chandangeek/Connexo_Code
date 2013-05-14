package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.core.Link;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
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
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.GasQuality;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.MeterInfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
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
    private IdentificationResponseStructure identificationStructure = null;
    private Link link;

    private static final int REF_DATE_DAYS_AHEAD = 0;
    public static final int LENGTH_CODE_PER_REQUEST = 1000; //Each Firmware upgrade request can contain up to 1000 bytes code.

    /**
     * @param link
     * @param logger
     * @param properties
     */
    public GprsRequestFactory(Link link, Logger logger, MTU155Properties properties, TimeZone timeZone) {
        this(link.getInputStream(), link.getOutputStream(), logger, properties, timeZone);
        this.link = link;
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
        this.link = link;
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
            this.properties = new MTU155Properties(new TypedProperties());
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
        request.setChannel(new Channel(1));
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
     * Creates a request to write registers in the meter.
     * @param validityDate: the validity date for the writing
     * @param wdb: a unique number
     * @param p_Session: the session configuration
     * @param attributeType: the fields of the object that needs to be written
     * @param rawData: the rawData with the objects
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
     * @throws com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException
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

        List<AbstractCTRObject> objects = Arrays.asList(registerResponse.getObjects());
        validateRegisterResponse(objects, objectId);
        return objects;
    }

    private void validateRegisterResponse(List<AbstractCTRObject> objects, CTRObjectID[] objectId) throws CTRException {
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
            }
        }
    }

    /** ****** SECTION 'FIRMWARE UPGRADING' ****** **/
    /**
     * Abort any previous ongoing download + initialize all download parameters with info of the new firmware image.
     * @param newSoftwareIdentifier Firmware version of the image
     * @param activationDate        Activation date of the image
     * @param size                  Total number of bytes of the image
     * @return
     * @throws CTRException
     */
    public void doInitFirmwareUpgrade(Identify newSoftwareIdentifier, Calendar activationDate, int size) throws CTRException {
        // 1. Abort previous download progress and re-initializes all download parameters.
        //Not needed - in some cases this doesn't work -- executing Step 2 contains an implicit re-init.

        // 2. Send initialization command with new identify and all init parameters in formatted code field.
        GPRSFrame response = getConnection().sendFrameGetResponse(getInitDownloadParametersRequest(newSoftwareIdentifier, activationDate, size));
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

    /**
     * Send out 1 segment of the firmware image code. The response is analysed to see if the segment gets acked.
     **/
   public boolean doSendFirmwareSegment(Identify newSoftwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment) throws CTRException {

       GPRSFrame response = getConnection().sendFrameGetResponse(getFirmwareCodeTransferRequest(newSoftwareIdentifier, firmwareUpgradeFile, lastAckedSegment));
       response.doParse();

       //Check if the response is an ACK
       Data downloadResponse;
       if (response.getData() instanceof AckStructure) {
           downloadResponse = response.getData();
           AckAdditionalDownloadData ackAdditionalDownloadData = new AckAdditionalDownloadData().parse(((AckStructure) downloadResponse).getAdditionalData().getBytes(), 0);

           // If The AckAdditionalDownloadData contains the segment number, we have guarantee the segment (and all previous ones) is correct received.
           int segmentSend = lastAckedSegment.getSegment() + 1;
           // System.out.println("Send out segment "+segmentSend +" to the meter. Meter acked segment :"+ ackAdditionalDownloadData.getSegment().getSegment()+".");

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
    private GPRSFrame getInitDownloadParametersRequest(Identify newSoftwareIdentifier, Calendar activationDate, int size) throws CTRException {
        Identify identify = newSoftwareIdentifier;
        identify.setIdentify(identify.getIdentify());
        Group group_s = new Group(25);      // Not meaningful for MTU155
        Group group_c = new Group(1);       // Different from 0
        Segment segment = new Segment(0);   //This will be the first segment

        // Construct special "Initialization code" field with all information of the new firmware
        AbstractCTRObject info = queryRegister("9.0.2");    // Equipment Identification Code object
        String CIA = info.getValue(0).getStringValue();     // CIA must be the Equipment Identification code

//        String VF = "R0." + ((newSoftwareIdentifier.getIdentify() < 0x100)    // E.g.: "R0.075"
//                ? "0" + ProtocolTools.getHexStringFromInt(newSoftwareIdentifier.getIdentify())
//                : ProtocolTools.getHexStringFromInt(newSoftwareIdentifier.getIdentify()));

//        String VF = "R0." + ((newSoftwareIdentifier.getIdentify() < 0x100)    // E.g.: "R0.075"
//                ? "0" + newSoftwareIdentifier.getHexIdentify()
//                : newSoftwareIdentifier.getHexIdentify());

        String VF = newSoftwareIdentifier.getHexIdentify();
        while (VF.length() < 6) {
            VF = "0" + VF;
        }

        InitialisationCode initialisationCode = new InitialisationCode(activationDate, CIA, VF, size);

        byte[] downloadRequest = ProtocolTools.concatByteArrays(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD).getBytes(),
                WriteDataBlock.getRandomWDB().getBytes(),
                identify.getBytes(),
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
     **/
    private GPRSFrame getFirmwareCodeTransferRequest(Identify softwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment) throws CTRParsingException {

        Group group_s = new Group(25);  // Not used for MTU155 - set to 25
        Group group_c = new Group(1);   // Not used for MTU155 - set to 1

        byte[] code = new byte[LENGTH_CODE_PER_REQUEST];
        int i = firmwareUpgradeFile.length - lastAckedSegment.getSegment() * LENGTH_CODE_PER_REQUEST;
        System.arraycopy(firmwareUpgradeFile, lastAckedSegment.getSegment() * LENGTH_CODE_PER_REQUEST, code, 0, (i > LENGTH_CODE_PER_REQUEST) ? LENGTH_CODE_PER_REQUEST : i);
        // E.g.: lastAckedSegment = 0 -> code will contain image bytes 0 to 999.
        //       lastAckedSegment = 1 -> code will contain image bytes 1000 -> 1999.

        Segment newSegmentNumber = new Segment(lastAckedSegment.getSegment() +1);   // Segment numbers are 1 based - 0 is reserved for special initialization segment.

        byte[] downloadRequest = ProtocolTools.concatByteArrays(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD).getBytes(),
                WriteDataBlock.getRandomWDB().getBytes(),
                softwareIdentifier.getBytes(),
                group_s.getBytes(),
                group_c.getBytes(),
                newSegmentNumber.getBytes(),
                code
        );
        GPRSFrame request = new GPRSFrame(true);
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getProfi().setLongFrame(true);

        request.getFunctionCode().setFunction(Function.DOWNLOAD);
        request.getStructureCode().setStructureCode(0);
        request.setChannel(new Channel(0));
        request.setData(new DownloadRequestStructure(true).parse(downloadRequest, 0));
        return request;
    }
    /** ****** END OF SECTION 'REQUEST MESSAGES SPECIFIC FOR FIRMWARE UPGRADING' ****** **/
    /** ****** END OF SECTION 'FIRMWARE UPGRADING' ****** **/

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
            throw new CTRNackException((NackStructure) response.getData());
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return executeResponse;
    }

    public Data executeRequest(CTRObjectID id, byte[] data) throws CTRException {
        return executeRequest(ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD), WriteDataBlock.getRandomWDB(), id, data);
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
            throw new CTRNackException((NackStructure) response.getData());
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return writeRegisterResponse;
    }

    /**
     *
     * @param attributeType: determines the fields of the objects
     * @param numberOfObjects: the number of objects to write to the device
     * @param rawData: the raw data containing the objects to write
     * @return: the meter's response (ack or nack)
     * @throws CTRException, if the meter's response was not recognized
     */
    public Data writeRegister(AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRException {
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
            writeRegisterResponse = (AckStructure) response.getData();
        } else if (response.getData() instanceof NackStructure) {
            throw new CTRNackException((NackStructure) response.getData());
        } else {
            throw new CTRException("Expected Ack or Nack but was " + response.getData().getClass().getSimpleName());
        }

        return writeRegisterResponse;
    }

    /**
     * Writes to register(s) in the meter, using the default and most used write parameters
     * DV = today + 14 days
     * WDB = random
     * PSession = 0x00 (open & close)
     * AttributeType = Value and ObjectID
     * @param objects: the objects that should be written in the meter
     * @return: the meter's response (ack or nack)
     * @throws CTRException, if the meter's response was not recognized
     */
    public Data writeRegister(AbstractCTRObject... objects) throws CTRException {
        return writeRegister(ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD), WriteDataBlock.getRandomWDB(), P_Session.getOpenAndClosePSession(), AttributeType.getValueAndObjectId(), objects);
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
    public ArrayEventsQueryResponseStructure queryEventArray(int index_Q) throws CTRException {
        return queryEventArray(new Index_Q(index_Q));
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
            if (getConnection() != null) {
                getConnection().sendFrameGetResponse(getEndOfSessionRequest());
            } else {
                getLogger().warning("Unable to close connection. getConnection() returned null.");
            }
        } catch (ComServerExecutionException e) {
            getLogger().severe("Failed to close session! " + e.getMessage());
            throw MdcManager.getComServerExceptionFactory().createProtocolDisconnectFailed(e);
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
    public IdentificationResponseStructure getIdentificationStructure() throws CTRException {
        if (identificationStructure == null) {
            identificationStructure = readIdentificationStructure();
        }
        return identificationStructure;
    }

    private Link getLink() {
        return link;
    }

    /**
     *
     * @return
     */
    public String getIPAddress() {
        //ToDo: getLink() will always return null! How should this be fixed?
        String ipAddress = null;
        if ((getLink() != null) && (getLink().getStreamConnection() != null) && (getLink().getStreamConnection().getSocket() != null)) {
            InetAddress address = getLink().getStreamConnection().getSocket().getInetAddress();
            if (address != null) {
                ipAddress = address.getHostAddress();
            }
        }
        return ipAddress == null ? "Unknown" : ipAddress;
    }

    /**
     * Get the cached meter info object. If not exist yet, create a new one.
     *
     * @return
     */
    public MeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new MeterInfo(this, getLogger(), getTimeZone());
        }
        return meterInfo;
    }

    /**
     * Get the cached GasQuality object. If not exist yet, create a new one.
     *
     * @return
     */
    public GasQuality getGasQuality() {
        if (gasQuality == null) {
            gasQuality = new GasQuality(this, getLogger());
        }
        return gasQuality;
    }

    public AbstractCTRObject queryRegister(String objectID) throws CTRException {
        List<AbstractCTRObject> objects = queryRegisters(new AttributeType(0x03), objectID);
        if ((objects != null) && (objects.size() > 0)) {
            return objects.get(0);
        } else {
            return null;
        }
    }
}