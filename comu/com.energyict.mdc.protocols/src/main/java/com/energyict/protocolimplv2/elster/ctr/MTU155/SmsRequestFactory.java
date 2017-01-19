package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.SecureSmsConnection;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Address;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.EncryptionStatus;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Function;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.StructureCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayEventsQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ExecuteRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.RegisterWriteRequestStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECFQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.CIA;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Counter_Q;
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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 8/03/12
 * Time: 15:01
 */
public class SmsRequestFactory implements RequestFactory {

    private final SecureSmsConnection connection;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private MTU155Properties properties;
    private Logger logger;
    private TimeZone timeZone;

    private static final int REF_DATE_DAYS_AHEAD = 0;
    private MeterInfo meterInfo;
    private int writeDataBlockID;
    private List<WriteDataBlock> writeDataBlockList;
    private boolean isEK155Protocol;

    public SmsRequestFactory(ComChannel comChannel, Logger logger, MTU155Properties properties, TimeZone timeZone, int writeDataBlockID, boolean isEK155Protocol, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.connection = new SecureSmsConnection(comChannel, properties, logger);
        this.logger = logger;
        this.properties = properties;
        this.timeZone = timeZone;
        this.writeDataBlockID = writeDataBlockID;
        this.isEK155Protocol = isEK155Protocol;
    }

    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException {
        WriteDataBlock newWriteDataBlock = getNewWriteDataBlock();
        writeDataBlockList.add(newWriteDataBlock);
        getConnection().sendFrameGetResponse(getRegisterWriteRequest(validityDate, newWriteDataBlock, p_Session, attributeType, objects));
        return null;
    }

    public Data writeRegister(AbstractCTRObject... objects) throws CTRException {
         return writeRegister(ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD), null, P_Session.getOpenAndClosePSession(), AttributeType.getValueAndObjectId(), objects);
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

    public Data executeRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRException {
        WriteDataBlock newWriteDataBlock = getNewWriteDataBlock();
        writeDataBlockList.add(newWriteDataBlock);
        getConnection().sendFrameGetResponse(getExecuteRequest(validityDate, newWriteDataBlock, id, data));
        return null;
    }

    public Data executeRequest(CTRObjectID id, byte[] data) throws CTRException {
        return executeRequest(ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD), null, id, data);
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

    public Data writeRegister(AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRException {
        WriteDataBlock newWriteDataBlock = getNewWriteDataBlock();
        writeDataBlockList.add(newWriteDataBlock);
        SMSFrame registerWriteRequest = getRegisterWriteRequest(
                ReferenceDate.getReferenceDate(REF_DATE_DAYS_AHEAD),
                newWriteDataBlock,
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

    public void sendEndOfSession() {
        getLogger().severe("End Of Session Request.");
    }

    /****************** GETTER AND SETTERS ******************/

    public SecureSmsConnection getConnection() {
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
            this.properties = new MTU155Properties(this.propertySpecService, this.thesaurus);
        }
        return properties;
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

    public Address getAddress() {
        return new Address(getProperties().getAddress());
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public WriteDataBlock getNewWriteDataBlock() {
        writeDataBlockID = (writeDataBlockID + 1) % 256;
        return new WriteDataBlock(writeDataBlockID);
    }

    public int getWriteDataBlockID() {
        return writeDataBlockID;
    }

    /**
     * The list of all used {@link WriteDataBlock}s for SMS messages.
     * When sending out an sms, each WriteDataBlock used, will automatically be added to this list, unless the list is reset.
     */
    public List<WriteDataBlock> getListOfWriteDataBlocks() {
        return this.writeDataBlockList;
    }

    /**
     * Clears the list of {@link WriteDataBlock}s used in SMS messages.
     */
    public void resetWriteDataBlockList() {
        this.writeDataBlockList = new ArrayList<>();
    }

    /****************** GETTER AND SETTERS ******************/


    /****************** METHODS NOT SUPPORTED AS SMS ******************/

    public List<AbstractCTRObject> getObjects(String... objectIDs) throws CTRConnectionException {
        throw new CTRConnectionException("SmsRequestFactory - getObjects method is not supported.");
    }

    public List<AbstractCTRObject> getObjects(CTRObjectID... objectIDs) throws CTRConnectionException {
        throw new CTRConnectionException("SmsRequestFactory - getObjects method is not supported.");
    }

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegisters method is not supported.");
    }

    public AbstractCTRObject queryRegister(String objectID) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegister method is not supported.");
    }

    public List<AbstractCTRObject> queryRegisters(CTRObjectID... objectId) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegisters method is not supported.");
    }

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, CTRObjectID... objectId) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryRegisters method is not supported.");
    }

    public TableDECFQueryResponseStructure queryTableDECF() throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTableDECF method is not supported.");
    }

    public TableDECQueryResponseStructure queryTableDEC() throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTableDEC method is not supported.");
    }

    public List<AbstractCTRObject> queryTrace(CTRObjectID id, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTrace method is not supported.");
    }

    public ArrayEventsQueryResponseStructure queryEventArray(int index_Q) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryEventArray method not supported.");
    }

    public ArrayEventsQueryResponseStructure queryEventArray(Index_Q index_Q) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryEventArray method not supported.");
    }

    public ArrayQueryResponseStructure queryArray(CTRObjectID objectID, Index_Q index_q, Counter_Q counter_q) throws CTRException {
        throw new CTRException("smsRequestFactory - queryArray method not supported.");
    }

    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRException {
        throw new CTRException("SmsRequestFactory - queryTrace_C method is not supported.");
    }

    public void doInitFirmwareUpgrade(Identify newSoftwareIdentifier, CIA cia, VF vf, Calendar activationDate, int size, boolean useLongFrameFormat) throws CTRException {
        throw new CTRException("SmsRequestFactory - doInitFirmwareUpgrade method is not supported.");
    }

    public boolean doSendFirmwareSegment(Identify newSoftwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment, boolean useLongFrames) throws CTRException {
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

    public boolean isEK155Protocol() {
        return isEK155Protocol;
    }

    public TableDECFQueryResponseStructure getTableDECF() throws CTRException {
        throw new CTRException("SmsRequestFactory - getTableDECF method is not supported.");
    }

    public TableDECQueryResponseStructure getTableDEC() throws CTRException {
        throw new CTRException("SmsRequestFactory - getTableDEC method is not supported.");
    }

    public GasQuality getGasQuality() throws CTRException {
        throw new CTRException("SmsRequestFactory - getGasQuality method is not supported.");
    }

}