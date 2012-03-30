package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureSmsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.SMSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.*;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.util.GasQuality;
import com.energyict.genericprotocolimpl.elster.ctr.util.MeterInfo;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;
import java.util.logging.Logger;

import static com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType.getValueAndObjectId;
import static com.energyict.genericprotocolimpl.elster.ctr.structure.field.P_Session.getOpenAndClosePSession;
import static com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate.getReferenceDate;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 8/03/12
 * Time: 15:01
 */
public class SmsRequestFactory implements RequestFactory{

    private final SecureSmsConnection connection;
    private MTU155Properties properties;
    private Logger logger;
    private TimeZone timeZone;
    private Link link;

    private static final int REF_DATE_DAYS_AHEAD = 0;
    private MeterInfo meterInfo;
    private int writeDataBlockID;

    public SmsRequestFactory(Link link, Logger logger, MTU155Properties properties, TimeZone timeZone, String phoneNumber, int writeDataBlockID) {
        this(logger, properties, timeZone, phoneNumber);
        this.link = link;
        this.writeDataBlockID = writeDataBlockID;
    }

    /**
     * @param logger
     * @param properties
     * @param timeZone
     */
    public SmsRequestFactory(Logger logger, MTU155Properties properties, TimeZone timeZone, String phoneNumber) {
        this.connection = new SecureSmsConnection(properties, logger, phoneNumber);
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
    }

    /**
     * Send out an SMS message: 'Write to register(s)'
     * @param validityDate: the validity date
     * @param wdb: a unique number
     * @param p_Session: the session configuration
     * @param attributeType: determines the fields of the objects
     * @param objects: the objects that should be written in the meter
     * @returns null
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException, if the meter's response was not recognized
     */
    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException {
        getConnection().sendFrameGetResponse(getRegisterWriteRequest(validityDate, getNewWriteDataBlock(), p_Session, attributeType, objects));
        return null;
    }

    /**
     * Writes to register(s) in the meter, using the default and most used write parameters
     * DV = today + 14 days
     * WDB = random
     * PSession = 0x00 (open & close)
     * AttributeType = Value and ObjectID
     *
     * @param objects: the objects that should be written in the meter
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's response was not recognized
     * @return: the meter's response (ack or nack)
     */
    public Data writeRegister(AbstractCTRObject... objects) throws CTRException {
         return writeRegister(getReferenceDate(REF_DATE_DAYS_AHEAD), null, getOpenAndClosePSession(), getValueAndObjectId(), objects);
    }


    /**
     * Creates a request to write registers in the meter.
     * @param validityDate: the validity date for the writing
     * @param wdb: a unique number
     * @param p_Session: the session configuration
     * @param attributeType: the fields of the object that needs to be written
     * @param objects: the objects that need to be written
     * @return a request structure
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException
     */
    public SMSFrame getRegisterWriteRequest(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRParsingException {
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

        SMSFrame request = new SMSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.WRITE);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.REGISTER);
        request.setData(new RegisterWriteRequestStructure(false).parse(writeRequest, 0));
        request.setWriteDataBlock(wdb);
        return request;
    }

     /**
     * Executes a certain request
     *
     * @param validityDate: the validity date
     * @param wdb:          a unique number
     * @param id:           the object's id
     * @param data:         data for the execute request
     * @return the meter's response (ack or nack)
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          when the meter's response was unexpected.
     */
    public Data executeRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRException {
        getConnection().sendFrameGetResponse(getExecuteRequest(validityDate, getNewWriteDataBlock(), id, data));
        return null;
    }

    public Data executeRequest(CTRObjectID id, byte[] data) throws CTRException {
        return executeRequest(getReferenceDate(REF_DATE_DAYS_AHEAD), null, id, data);
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
    public SMSFrame getExecuteRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRParsingException {
        byte[] executeRequest = ProtocolTools.concatByteArrays(
                getPassword(),
                validityDate.getBytes(),
                wdb.getBytes(),
                id.getBytes(),
                data
        );

        SMSFrame request = new SMSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.EXECUTE);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(0);
        request.setData(new ExecuteRequestStructure(false).parse(executeRequest, 0));
        request.setWriteDataBlock(wdb);
        return request;
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
     * @param attributeType:   determines the fields of the objects
     * @param numberOfObjects: the number of objects to write to the device
     * @param rawData:         the raw data containing the objects to write
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's response was not recognized
     * @return: the meter's response (ack or nack)
     */
    public Data writeRegister(AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRException {
        SMSFrame registerWriteRequest = getRegisterWriteRequest(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD),
                getNewWriteDataBlock(),
                P_Session.getOpenAndClosePSession(),
                attributeType,
                numberOfObjects,
                rawData
        );
        getConnection().sendFrameGetResponse(registerWriteRequest);
        return null;
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
    private SMSFrame getRegisterWriteRequest(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRParsingException {
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

        SMSFrame request = new SMSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.WRITE);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.REGISTER);
        request.setData(new RegisterWriteRequestStructure(false).parse(writeRequest, 0));
        request.setWriteDataBlock(wdb);
        return request;
    }

    /**
     * Send an end of session to the device
     */
    public void sendEndOfSession() {
        getLogger().severe("End Of Session Request.");
    }

    /****************** GETTER AND SETTERS ******************/
    /**
     * Getter for the connection
     *
     * @return
     */
    public SecureSmsConnection getConnection() {
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

    public MeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new MeterInfo(this, getLogger(), getTimeZone());
        }
        return meterInfo;
    }

    /**
     * Create a new address, with a value from the protocolProperties
     *
     * @return
     */
    public Address getAddress() {
        return new Address(getProperties().getAddress());
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Link getLink() {
        return link;
    }

    public WriteDataBlock getNewWriteDataBlock() {
        writeDataBlockID = (writeDataBlockID + 1) % 256;
        return new WriteDataBlock(writeDataBlockID);
    }

    public int getWriteDataBlockID() {
        return writeDataBlockID;
    }

    /****************** GETTER AND SETTERS ******************/


    /****************** METHODS NOT SUPPORTED AS SMS ******************/

    /**
     * Requests a number of objects from the meter, via a query.
     *
     * @param attributeType: determines the fields of the objects in the meter's response
     * @param objectId:      the id's of the requested objects
     * @return a list of objects, as requested
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException
     *
     */
    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegisters method is not supported.");
    }

    public AbstractCTRObject queryRegister(String objectID) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegister method is not supported.");
    }

    /**
     * Returns a list of requested objects
     *
     * @param attributeType: determines the fields of the objects
     * @param objectId:      the id's of the requested objects
     * @return list of requested objects
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException
     *
     */
    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, CTRObjectID... objectId) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegisters method is not supported.");
    }

    /**
     * Queries for a decf table. Returns the meter response.
     *
     * @return the meter response, being a decf table
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's answer was not a decf table
     */
    public TableDECFQueryResponseStructure queryTableDECF() throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTableDECF method is not supported.");

    }

    /**
     * Queries for a dec table. Returns the meter response.
     *
     * @return the meter response, being a dec table
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's answer was not a dec table
     */
    public TableDECQueryResponseStructure queryTableDEC() throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTableDEC method is not supported.");
    }

    /**
     * Do a trace query
     *
     * @param id:               the id of the object you need interval data from
     * @param period:           the interval period (e.g.: 15 minutes)
     * @param startDate:        the start date
     * @param numberOfElements: the number of interval values returned
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          when the meter's response was not recognized
     * @return: a list of objects
     */
    public List<AbstractCTRObject> queryTrace(CTRObjectID id, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTrace method is not supported.");
    }

    /**
     * Queries for a number of events
     *
     * @param index_Q: number of events to query for
     * @return the meter's response containing event records
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's response was not recognized
     */
    public ArrayEventsQueryResponseStructure queryEventArray(int index_Q) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryEventArray method not supported.");
    }

    /**
     * Queries for a number of events
     *
     * @param index_Q: number of events to query for
     * @return the meter's response containing event records
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's response was not recognized
     */
    public ArrayEventsQueryResponseStructure queryEventArray(Index_Q index_Q) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryEventArray method not supported.");
    }

    /**
     * Do a trace_C query
     *
     * @param id:            the id of the requested object
     * @param period:        the interval period
     * @param referenceDate: the reference date
     * @return the meter's response
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException,
     *          if the meter's response was not recognized
     */
    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTrace_C method is not supported.");
    }

    /**
     * Abort any previous ongoing download + initialize all download parameters with info of the new firmware image.
     *
     * @param newSoftwareIdentifier Firmware version of the image
     * @param activationDate        Activation date of the image
     * @param size                  Total number of bytes of the image
     * @return
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException
     *
     */
    public void doInitFirmwareUpgrade(Identify newSoftwareIdentifier, Calendar activationDate, int size) throws CTRException {
        throw new CTRException("SmsRequestFactory - doInitFirmwareUpgrade method is not supported.");
    }

    /**
     * Send out 1 segment of the firmware image code. The response is analysed to see if the segment gets acked.
     */
    public boolean doSendFirmwareSegment(Identify newSoftwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment) throws CTRException {
        throw new CTRException("SmsRequestFactory - doSendFirmwareSegment method is not supported.");
    }

    public IdentificationResponseStructure getIdentificationStructure() {
        getLogger().severe("SmsRequestFactory - getIdentificationStructure method is not supported.");
        return null;
    }

    public String getIPAddress()  {
        getLogger().severe("SmsRequestFactory - getIPAddress method is not supported.");
        return null;
    }

    /**
     * Get the cached GasQuality object. If not exist yet, create a new one.
     *
     * @return
     */
    public GasQuality getGasQuality() throws CTRException {
        throw new CTRException("SmsRequestFactory - getGasQuality method is not supported.");
    }

     /****************** METHODS NOT SUPPORTED AS SMS ******************/
}