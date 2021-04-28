package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.dlms.cosem.CosemObject;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
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
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.siemenszmd.LogBookReader;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.messaging.ZMDMessages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 13/12/11
 * Time: 16:02
 */
public class ZMD extends AbstractSmartDlmsProtocol implements MessageProtocol, ProtocolLink, SerialNumberSupport {
    protected static final ObisCode[] SerialNumberSelectionObjects = {
            // Identification numbers 1.1, 1.2, 1.3 and 1.4
            ObisCode.fromString("1.0.0.0.0.255"), ObisCode.fromString("1.0.0.0.1.255"), ObisCode.fromString("1.0.0.0.2.255"), ObisCode.fromString("1.0.0.0.3.255"),
            // Identification numbers 2.1 and 2.2
            ObisCode.fromString("0.0.96.1.0.255"), ObisCode.fromString("0.0.96.1.1.255"),
            // Connection ID, Parametrisation ID and Configuration ID
            ObisCode.fromString("0.0.96.2.1.255"), ObisCode.fromString("0.1.96.2.5.255"), ObisCode.fromString("0.1.96.2.2.255")
    };

    private final PropertySpecService propertySpecService;
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

    public ZMD(DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        this.messageProtocol = new ZMDMessages(this, messageFileFinder, messageFileExtractor);
    }

    @Override
    public ZMDProperties getProperties() {
        if (properties == null) {
            properties = new ZMDProperties(this.propertySpecService);
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

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            throw new ConnectionException(ProtocolTools.format("Failed while initializing the DLMS connection: {0}.", new Object[]{e.getMessage()}));
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

    @Override
    protected void initAfterConnect() throws ConnectionException {
    }

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

    @Override
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

    @Override
    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            UniversalObject uo = getMeterConfig().getVersionObject();
            firmwareVersion = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
        }
        return firmwareVersion;
    }

    @Override
    public String getMeterSerialNumber() throws ConnectionException {

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

    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return ObisCodeMapper.getRegisterInfo(register.getObisCode());
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().read(registers);
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return getLogBookReader().getMeterEvents(lastLogbookDate);
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    public LogBookReader getLogBookReader() {
        if (logBookReader == null) {
            logBookReader = new LogBookReader(this, getCosemObjectFactory());
        }
        return logBookReader;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr ICG Family DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-01-13 14:10:00 +0100 (We, 13 Jan 2021)$";
    }

    public void resetDemand() throws IOException {
        GenericInvoke gi = new GenericInvoke(this, new ObjectReference(getMeterConfig().getObject(new DLMSObis(ObisCode.fromString("0.0.240.1.0.255").getLN(), (short) 10100, (short) 0)).getBaseName()), 6);
        gi.invoke(new Integer8(0).getBEREncodedByteArray());
    }

    @Override
    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {
        // no additional validation is done
    }

    @Override
    public void applyMessages(List<MessageEntry> messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }

    public int getDstFlag() {
        return dstFlag;
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return getDlmsSession().getDLMSConnection();
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    @Override
    public boolean isRequestTimeZone() {
        return (getProperties().getRequestTimeZone() != 0);
    }

    @Override
    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    @Override
    public int getReference() {
        return getProperties().getReference().getReference();
    }

    @Override
    public StoredValues getStoredValues() {
        if (storedValuesImpl == null) {
            storedValuesImpl = new StoredValuesImpl(getCosemObjectFactory());
        }
        return storedValuesImpl;
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            CosemObject cosemObject = getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode());
            if (cosemObject != null) {
                iConfigProgramChange = (int) cosemObject.getValue();
            }
        }
        return iConfigProgramChange;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getMeterSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }


    public void setRegister(String name, String value) throws IOException {
        boolean classSpecified = false;
        if (name.indexOf(':') >= 0) {
            classSpecified = true;
        }
        final DLMSObis ln = new DLMSObis(name);
        if ((ln.isLogicalName()) && (classSpecified)) {
            getCosemObjectFactory().getGenericWrite(ObisCode.fromByteArray(ln.getLN()), ln.getOffset(), ln.getDLMSClass()).write(convert(value));
        } else {
            throw new NoSuchRegisterException("GenericGetSet, setRegister, register " + name + " does not exist.");
        }
    }

    /**
     * Converts the given string.
     *
     * @param s The string.
     * @return
     */
    private byte[] convert(final String s) {
        if ((s.length() % 2) != 0) {
            throw new IllegalArgumentException("String length is not a modulo 2 hex representation!");
        } else {
            final byte[] data = new byte[s.length() / 2];

            for (int i = 0; i < (s.length() / 2); i++) {
                data[i] = (byte) Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16);
            }

            return data;
        }
    }
}
