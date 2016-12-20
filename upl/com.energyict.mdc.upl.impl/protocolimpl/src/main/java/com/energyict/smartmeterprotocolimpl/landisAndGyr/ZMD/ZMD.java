package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.cbo.Utils;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
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
import com.energyict.protocol.DemandResetProtocol;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.siemenszmd.LogBookReader;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
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
public class ZMD extends AbstractSmartDlmsProtocol implements DemandResetProtocol, MessageProtocol, ProtocolLink, SerialNumberSupport {

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
    private LogBookReader logBookReader;
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
            throw new ConnectionException(Utils.format("Failed while initializing the DLMS connection: {0}.", new Object[]{e.getMessage()}));
        }
        HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
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

    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            UniversalObject uo = getMeterConfig().getVersionObject();
            firmwareVersion = getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
        }
        return firmwareVersion;
    }

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

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().read(registers);
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
       return getLogBookReader().getMeterEvents(lastLogbookDate);
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

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

    /**
     * Returns the protocol version date
     */
    public String getVersion() {
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
    }

    public void resetDemand() throws IOException {
        GenericInvoke gi = new GenericInvoke(this, new ObjectReference(getMeterConfig().getObject(new DLMSObis(ObisCode.fromString("0.0.240.1.0.255").getLN(), (short)10100, (short)0)).getBaseName()),6);
        gi.invoke(new Integer8(0).getBEREncodedByteArray());
    }

    @Override
    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {
        getProtocolProperties().validateProperties();
    }

    public void applyMessages(List messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

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

    public DLMSConnection getDLMSConnection() {
        return getDlmsSession().getDLMSConnection();
    }

    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    public boolean isRequestTimeZone() {
        return (getProperties().getRequestTimeZone() != 0);
    }

    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    public int getReference() {
        return getProperties().getReference().getReference();
    }

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

    @Override
    public String getSerialNumber() {
        try {
            return getMeterSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }
}