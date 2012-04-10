package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.Register;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging.ZMDMessages;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 13/12/11
 * Time: 16:02
 */
public class ZMD extends AbstractSmartDlmsProtocol implements DemandResetProtocol, MessageProtocol, ProtocolLink, TimeOfUseMessaging {

    protected static final ObisCode[] SerialNumberSelectionObjects = {
            // Identification numbers 1.1, 1.2, 1.3 and 1.4
            ObisCode.fromString("1.0.0.0.0.255"), ObisCode.fromString("1.0.0.0.1.255"), ObisCode.fromString("1.0.0.0.2.255"), ObisCode.fromString("1.0.0.0.3.255"),
            // Identification numbers 2.1 and 2.2
            ObisCode.fromString("0.0.96.1.0.255"), ObisCode.fromString("0.0.96.1.1.255"),
            // Connection ID, Parametrisation ID and Configuration ID
            ObisCode.fromString("0.0.96.2.1.255"), ObisCode.fromString("0.1.96.2.5.255"), ObisCode.fromString("0.1.96.2.2.255")
    };

    protected String firmwareVersion;

    private CosemObjectFactory cosemObjectFactory = null;
    private StoredValuesImpl storedValuesImpl = null;

    private RegisterReader registerReader;
    private LoadProfileBuilder loadProfileBuilder;

    private int dstFlag;

    // lazy initializing
    private int iMeterTimeZoneOffset = 255;
    private int iConfigProgramChange = -1;

    // Added for MeterProtocol interface implementation
    private ZMDProperties properties = null;

    private final ZMDMessages messageProtocol;

    public ZMD() {
        this.messageProtocol = new ZMDMessages(this);
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    protected ZMDProperties getProperties() {
         if (properties == null) {
            properties = new ZMDProperties();
        }
        return properties;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        super.init(inputStream, outputStream, timeZone, logger);
        dstFlag = -1;
        iMeterTimeZoneOffset = 255; // Lazy initializing
        iConfigProgramChange = -1; // Lazy initializing
        cosemObjectFactory = new CosemObjectFactory(this);
        storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
    }

    protected SecurityProvider getSecurityProvider() {
        return  getProperties().getSecurityProvider();
    }

    /**
     * Getter for the context ID
     *
     * @return the context ID
     */
    protected int getContextId() {
        if (getReference() == ProtocolLink.LN_REFERENCE) {
            return (getProperties().getDataTransportSecurityLevel() == 0) ? AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING :
                    AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
        } else if (getReference() == ProtocolLink.SN_REFERENCE) {
            return (getProperties().getDataTransportSecurityLevel() == 0) ? AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING :
                    AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING;
        } else {
            throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
        }
    }

    /**
     * Returns a boolean whether or not there's encryption used
     *
     * @return boolean
     */
    protected boolean isCiphered() {
        return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
    }

    /**
     * Configure the {@link com.energyict.dlms.aso.ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     *         the default value({@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    protected ConformanceBlock configureConformanceBlock() {
        return new ConformanceBlock(1573408L);
    }

    protected byte[] getSystemIdentifier() {
        return null;
    }

    /**
     * Initialization method right after we are connected to the physical device.
     */
    @Override
    protected void initAfterConnect() throws ConnectionException {
        validateSerialNumber();
    }

    private void validateSerialNumber() throws ConnectionException {
        if ((getProperties().getSerialNumber() == null) || ("".compareTo(getProperties().getSerialNumber()) == 0)) {
            return;
        }
        String sn = getSerialNumber();
        if ((sn != null) && (sn.compareTo(getProperties().getSerialNumber()) == 0)) {
            return;
        }
        throw new ConnectionException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + getProperties().getSerialNumber());
    }

    public String getSerialNumber() throws ConnectionException {
        /** The serial number is present in a reserved object: COSEM Logical device name object
         * In order to facilitate access using SN referencing, this object has a reserved short name by DLMS/COSEM convention: 0xFD00.
         * See topic  'Reserved base_names for special COSEM objects' in the DLMS Blue Book.
        **/
        try {
            return getCosemObjectFactory().getGenericRead(0xFD00, DLMSUtils.attrLN2SN(2)).getString();
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new ConnectionException("Could not retrieve the Serial number object." + e);
        }
    }

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    @Override
    public Date getTime() throws IOException {
        try {
            Clock clock = this.dlmsSession.getCosemObjectFactory().getClock();
            Date dateTime = clock.getDateTime();
            dstFlag = clock.getDstFlag();
            return dateTime;
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the Clock object." + e);
        }
    }

    /**
     * <p>
     * sets the device time to the current system time.
     * </p>
     *
     * @param newMeterTime the time to set in the meter
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void setTime(Date newMeterTime) throws IOException {
        Calendar calendar = null;
        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
                calendar = ProtocolUtils.getCalendar(false, requestTimeZone());
            } else if (dstFlag == 1) {
                calendar = ProtocolUtils.getCalendar(true, requestTimeZone());
            } else {
                throw new IOException("setTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            calendar = ProtocolUtils.initCalendar(false, getTimeZone());
        }

        calendar.add(Calendar.MILLISECOND, getProperties().getRoundTripCorrection());
        doSetTime(calendar);
    }

     protected int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
            iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
        }
        return iMeterTimeZoneOffset;
    }

    private void doSetTime(Calendar calendar) throws IOException {
        //byte[] responseData;
        byte[] byteTimeBuffer = new byte[14];
        int i;

//      byteTimeBuffer[0]=1;  This caused an extra 0x01 in the requestBuffer
//      DLMS code has changed (read -> corrected) which causes this to be obsolete

        byteTimeBuffer[0] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
        byteTimeBuffer[1] = 12; // length
        byteTimeBuffer[2] = (byte) (calendar.get(calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) calendar.get(calendar.YEAR);
        byteTimeBuffer[4] = (byte) (calendar.get(calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) calendar.get(calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) calendar.get(calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) calendar.get(calendar.MINUTE);
        byteTimeBuffer[9] = (byte) calendar.get(calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0xFF;
        byteTimeBuffer[11] = (byte) 0x80;
        byteTimeBuffer[12] = 0x00;

        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
                byteTimeBuffer[13] = 0x00;
            } else if (dstFlag == 1) {
                byteTimeBuffer[13] = (byte) 0x80;
            } else {
                throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            if (getTimeZone().inDaylightTime(calendar.getTime())) {
                byteTimeBuffer[13] = (byte) 0x80;
            } else {
                byteTimeBuffer[13] = 0x00;
            }
        }

        getCosemObjectFactory().getGenericWrite((short) getDlmsSession().getMeterConfig().getClockSN(), DLMSCOSEMGlobals.TIME_TIME).write(byteTimeBuffer);

    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     * @throws java.io.IOException Thrown in case of an exception
     */
    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            UniversalObject uo = getMeterConfig().getVersionObject();
            firmwareVersion = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
        }
        return firmwareVersion;
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws java.io.IOException thrown in case of an exception
     */
    public String getMeterSerialNumber() throws IOException {
        return getSerialNumber();
    }

    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public RegisterInfo translateRegister(Register register) throws IOException {
        return ObisCodeMapper.getRegisterInfo(register.getObisCode());
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().read(registers);
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    /**
     * Get all the meter events from the device starting from the given date.
     *
     * @param lastLogbookDate the date of the last <CODE>MeterEvent</CODE> stored in the database
     * @return a list of <CODE>MeterEvents</CODE>
     * @throws java.io.IOException when a logical error occurred
     */
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastLogbookDate);
        return getEventLog(cal);
    }

    /**
     * Getter for the MeterEvent list
     *
     * @param fromCalendar the time to start collection events from the device
     * @return a list of MeterEvents
     */
    public List<MeterEvent> getEventLog(Calendar fromCalendar) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        Calendar toCalendar = ProtocolUtils.getCalendar(getTimeZone()); // Must be in the device time zone
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCalendar, toCalendar);

        int index = 0;
        int eventIdIndex = getProperties().getEventIdIndex();

        if (eventIdIndex == -1) {
            Iterator it = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getCaptureObjects().iterator();
            while (it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject) it.next();
                if (capturedObject.getLogicalName().getObisCode().equals(ObisCode.fromString("0.0.96.240.12.255")) &&
                        (capturedObject.getAttributeIndex() == 2) &&
                        (capturedObject.getClassId() == 3)) {
                    break;
                } else {
                    index++;
                }
            }
        }

        for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
            Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
            int id = 0;
            if (eventIdIndex == -1) {
                id = dc.getRoot().getStructure(i).getInteger(index);
            } else {
                id = dc.getRoot().getStructure(i).convert2Long(eventIdIndex).intValue();
            }
            MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
            if (meterEvent != null) {
                meterEvents.add(meterEvent);
            }
        }
        return meterEvents;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the meter.
     * Build up a list of <CODE>LoadProfileConfiguration</CODE> objects and return them so the
     * framework can validate them to the configuration in EIServer
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>LoadProfileConfiguration</CODE> objects corresponding with the meter
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        List<LoadProfileConfiguration> loadProfileConfigurations = getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
        return loadProfileConfigurations;
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date$";
    }

    /**
     * Execute a billing reset on the device. After receiving the �Demand Reset�
     * command the meter executes a demand reset by doing a snap shot of all
     * energy and demand registers.
     *
     * @throws java.io.IOException
     */
    public void resetDemand() throws IOException {
        GenericInvoke gi = new GenericInvoke(this, new ObjectReference(getMeterConfig().getObject(new DLMSObis(ObisCode.fromString("0.0.240.1.0.255").getLN(), (short)10100, (short)0)).getBaseName()),6);
        gi.invoke(new Integer8(0).getBEREncodedByteArray());
    }

    @Override
    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {
        getProtocolProperties().validateProperties();
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(List messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }

    public int getDstFlag() {
        return dstFlag;
    }

    /**
     * Getter for property {@link com.energyict.dlms.DLMSConnection}.
     *
     * @return the {@link com.energyict.dlms.DLMSConnection}.
     */
    public DLMSConnection getDLMSConnection() {
        return getDlmsSession().getDLMSConnection();
    }

    /**
     * Getter for property meterConfig.
     *
     * @return Value of property meterConfig.
     */
    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    /**
     * Check if the {@link java.util.TimeZone} is read from the DLMS device, or if the
     * {@link java.util.TimeZone} from the {@link com.energyict.protocol.MeterProtocol} should be used.
     *
     * @return true is the {@link java.util.TimeZone} is read from the device
     */
    public boolean isRequestTimeZone() {
        return (getProperties().getRequestTimeZone() != 0);
    }

    /**
     * Getter for the round trip correction.
     *
     * @return the value of the round trip correction
     */
    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    /**
     * Getter for the type of reference used in the DLMS protocol. This can be
     * {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE or {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE
     *
     * @return {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE for short name or
     *         {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE for long name
     */
    public int getReference() {
        return getProperties().getReference().getReference();
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.StoredValues} object
     *
     * @return the {@link com.energyict.dlms.cosem.StoredValues} object
     */
    public StoredValues getStoredValues() {
        if (storedValuesImpl == null) {
            storedValuesImpl = new StoredValuesImpl(getCosemObjectFactory());
        }
        return storedValuesImpl;
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            iConfigProgramChange = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return iConfigProgramChange;
    }

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return this.messageProtocol.getTimeOfUseMessageBuilder();
    }

    /**
     * Get the TimeOfUseMessagingConfig object that contains all the capabilities for the current protocol
     *
     * @return the config object
     */
    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        return this.messageProtocol.getTimeOfUseMessagingConfig();
    }

}