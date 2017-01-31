/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DemandResetProtocol;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.events.LogBookReader;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging.ZMDMessages;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZMD extends AbstractSmartDlmsProtocol implements DemandResetProtocol, MessageProtocol, ProtocolLink {

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr ICG Family DLMS";
    }

    protected String firmwareVersion;

    private CosemObjectFactory cosemObjectFactory = null;
    private StoredValuesImpl storedValuesImpl = null;

    private RegisterReader registerReader;
    private LogBookReader logBookReader;
    private LoadProfileBuilder loadProfileBuilder;

    private int dstFlag;

    // lazy initializing
    private int iMeterTimeZoneOffset = 255;
    private int iConfigProgramChange = -1;

    // Added for MeterProtocol interface implementation
    private ZMDProperties properties = null;

    private final ZMDMessages messageProtocol;

    @Inject
    public ZMD(PropertySpecService propertySpecService, OrmClient ormClient, CalendarService calendarService, DeviceMessageFileService deviceMessageFileService) {
        super(propertySpecService, ormClient);
        this.messageProtocol = new ZMDMessages(this, calendarService, deviceMessageFileService);
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public ZMDProperties getProperties() {
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

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                            //HDLC:         9600 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "", 0);      //IEC1107:      300 baud, 7E1
//        getDlmsSession().getDLMSConnection().setSNRMType(1);
    }

    protected SecurityProvider getSecurityProvider() {
        return getProperties().getSecurityProvider();
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
        String configuredSerial = getProperties().getSerialNumber();
        configuredSerial = configuredSerial.toLowerCase().startsWith("lgz") ? configuredSerial.substring(3) : configuredSerial;
        if ((sn != null) && (sn.compareTo(configuredSerial) == 0)) {
            return;
        }
        throw new ConnectionException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + configuredSerial);
    }

    public String getSerialNumber() throws ConnectionException {
        /** The serial number is present in a reserved object: COSEM Logical device name object
         * In order to facilitate access using SN referencing, this object has a reserved short name by DLMS/COSEM convention: 0xFD00.
         * See topic  'Reserved base_names for special COSEM objects' in the DLMS Blue Book.
        **/
        try {
            String retrievedSerial = getCosemObjectFactory().getGenericRead(0xFD00, DLMSUtils.attrLN2SN(2)).getString();
            if (retrievedSerial.toLowerCase().startsWith("lgz")) {
                return retrievedSerial.substring(3);
            } else {
                return retrievedSerial;
            }
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
        this.dlmsSession.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(newMeterTime, getTimeZone()));
    }

    @Override
    public TimeZone getTimeZone() {
        if (isRequestTimeZone()) {
            Calendar calendar = ProtocolUtils.getCalendar(dstFlag == 1, 1);
            return calendar.getTimeZone();
        } else {
            return super.getTimeZone();
        }
    }

    protected int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
            iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
        }
        return iMeterTimeZoneOffset;
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
       return getLogBookReader().getMeterEvents(lastLogbookDate);
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
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

    public LogBookReader getLogBookReader() {
        if (logBookReader == null) {
            logBookReader = new LogBookReader(this);
        }
        return logBookReader;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    /**
     * Returns the protocol version
     */
    public String getVersion() {
        return "$Date: 2014-09-23 11:13:20 +0200 (Tue, 23 Sep 2014) $";
    }

    /**
     * Execute a billing reset on the device. After receiving the 'Demand Reset'
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
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
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
     * {@link java.util.TimeZone} from the {@link MeterProtocol} should be used.
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
}