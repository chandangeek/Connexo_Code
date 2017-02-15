package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.RegisterCache;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

public abstract class AbstractDLMS extends AbstractProtocol implements ProtocolLink, MessageProtocol, EventMapper, RegisterCache, SerialNumberSupport {

    private static final int EMETER_NR_OF_CHANNELS = 4;
    static final String PROPERTY_LP_MULTIPLIER = "ApplyLoadProfileMultiplier";

    private WaveFlowDLMSWMessages waveFlowDLMSWMessages = new WaveFlowDLMSWMessages(this);

    /**
     * Command 31 to transparantly request an obis code
     */
    private TransparantObjectAccessFactory transparantObjectAccessFactory;

    private String serialNumberA;

    private ObisCodeMapper obisCodeMapper;

    /**
     * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
     */
    private int correctTime;

    /**
     * the correctWaveflowTime property. The waveflow time will be set also with the setTime() mechanism as a default
     */
    private int correctWaveflowTime;

    private int maxNumberOfIntervals = 0;
    /**
     * the load profile obis code custom property
     */
    private ObisCode loadProfileObisCode;

    /**
     * reference to the radio commands factory
     */
    private RadioCommandFactory radioCommandFactory;

    /**
     * In case we want to readout meta data in the meter to validate the configuration, set this custom property to true.
     */
    private boolean verifyProfileInterval = false;

    /**
     * Indicates if the AM700 module uses old or new firmware.
     * Default is new firmware behaviour
     */
    private boolean isOldFirmware = false;

    /**
     * Indicates whether or not to use the simple contactor change method
     */
    private boolean optimizeChangeContactorStatus = false;

    /**
     * Reference to the parameter factory
     */
    private ParameterFactory parameterFactory;

    /**
     * the encryptor for the data
     */
    private Encryption encryptor = new Encryption(null, getLogger());

    /**
     * reference to the lower connect latyers of the wavenis stack
     */
    private WaveFlowConnect waveFlowConnect;

    public AbstractDLMS(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    enum PairingMeterId {
        AS253(1),
        AS1253(2),
        A1800(3);

        private final int id;

        final int getId() {
            return id;
        }

        PairingMeterId(final int id) {
            this.id = id;
        }
    }

    @Override
    protected void doConnect() throws IOException {
    }

    protected abstract ObisCode getSerialNumberObisCodeForPairingRequest();

    protected abstract ObisCode getUtilityIdObiscode();

    protected abstract Map<ObisCode, ObjectEntry> getObjectEntries();

    protected abstract PairingMeterId getPairingMeterId();

    ObjectEntry findObjectByObiscode(final ObisCode obisCode) throws NoSuchRegisterException {
        ObjectEntry o = getObjectEntries().get(obisCode);
        if (o == null) {
            throw new NoSuchRegisterException("Register with obiscode [" + obisCode + "] not found.");
        } else {
            return o;
        }
    }

    boolean isOptimizeChangeContactorStatus() {
        return optimizeChangeContactorStatus;
    }

    Entry<ObisCode, ObjectEntry> findEntryByDescription(final String description) throws NoSuchRegisterException {
        for (Entry<ObisCode, ObjectEntry> o : getObjectEntries().entrySet()) {
            if (o.getValue().getDescription().compareTo(description) == 0) {
                return o;
            }
        }

        throw new NoSuchRegisterException("Register with description [" + description + "] not found.");
    }

    public int getMaxNumberOfIntervals() {
        return maxNumberOfIntervals;
    }

    /**
     * Check if the AM700 board has old (1.0.X or lower) firmware
     * This is important for the parsing of the event code
     */
    public boolean isOldFirmware() {
        return isOldFirmware;
    }

    public final ParameterFactory getParameterFactory() {
        return parameterFactory;
    }

    public final RadioCommandFactory getRadioCommandFactory() {
        return radioCommandFactory;
    }

    public final TransparantObjectAccessFactory getTransparantObjectAccessFactory() {
        return transparantObjectAccessFactory;
    }

    final Encryption getEncryptor() {
        return encryptor;
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    private byte[] buildPairingFrame(int baudrate) throws IOException {

        byte[] pairingFrame = new byte[]{(byte) 0x30, (byte) 0x02, (byte) 0x02, (byte) 0x11,
                (byte) 0x02, (byte) 0x10, (byte) 0x57, (byte) 0x00, (byte) 0x00,
                (byte) 0x08, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x60, (byte) 0x01, (byte) 0x00, (byte) 0xFF, (byte) 0x02};


        int METER_SERIAL_OBISCODE_OFFSET = 32;

        byte[] ln = getSerialNumberObisCodeForPairingRequest().getLN();
        for (int i = 0; i < 6; i++) {
            pairingFrame[METER_SERIAL_OBISCODE_OFFSET + i] = ln[i];
        }

        pairingFrame[1] = (byte) getPairingMeterId().getId();

        pairingFrame[2] = 2; // 19200 baud
        if (baudrate == 9600) {
            pairingFrame[2] = 1; // 9600 baud
        }

        int OFFSET_PASSWORD_LENGTH = 9;
        int OFFSET_PASSWORD = 10;
        int OFFSET_ADDRESS_LENGTH = 4;
        int OFFSET_ADDRESS = 5;

        String address = getInfoTypeDeviceID();
        if ((address != null) && (address.compareTo("") != 0)) {
            getLogger().info("Build pairingrequest frame with device address [" + address + "]");
            long addr = Long.parseLong(address);

            if (addr >= 0x100000000L) {
                throw new IOException("Address > maxint!!");
            } else if (addr >= 0x1000000L) {
                pairingFrame[OFFSET_ADDRESS_LENGTH] = 4;
                pairingFrame[OFFSET_ADDRESS] = (byte) (addr >> 24);
                pairingFrame[OFFSET_ADDRESS + 1] = (byte) (addr >> 16);
                pairingFrame[OFFSET_ADDRESS + 2] = (byte) (addr >> 8);
                pairingFrame[OFFSET_ADDRESS + 3] = (byte) (addr);
            } else if (addr >= 0x10000L) {
                pairingFrame[OFFSET_ADDRESS_LENGTH] = 3;
                pairingFrame[OFFSET_ADDRESS] = (byte) (addr >> 16);
                pairingFrame[OFFSET_ADDRESS + 1] = (byte) (addr >> 8);
                pairingFrame[OFFSET_ADDRESS + 2] = (byte) (addr);
            } else {
                pairingFrame[OFFSET_ADDRESS_LENGTH] = 2;
                pairingFrame[OFFSET_ADDRESS] = (byte) (addr >> 8);
                pairingFrame[OFFSET_ADDRESS + 1] = (byte) (addr);
            }

            pairingFrame[OFFSET_ADDRESS] = (byte) (addr >> 8);
            pairingFrame[OFFSET_ADDRESS + 1] = (byte) (addr);
        } else {
            return null;
        }

        String password = getInfoTypePassword();
        if ((password != null) && (password.compareTo("") != 0)) {
            getLogger().info("Build pairingrequest frame with password [" + password + "]");
            if (password.length() == 40) {
                pairingFrame[OFFSET_PASSWORD_LENGTH] = 20;
                //convert to byte values
                for (int i = 0; i < 40; i += 2) {
                    int val = Integer.parseInt(password.substring(i, i + 2));
                    pairingFrame[OFFSET_PASSWORD + (i / 2)] = (byte) val;
                }
            } else if (password.length() > 20) {
                throw new IOException("Password length > 20 characters!");
            } else {
                byte[] pw = password.getBytes();
                pairingFrame[OFFSET_PASSWORD_LENGTH] = (byte) pw.length;
                for (int i = 0; i < pw.length; i++) {
                    pairingFrame[OFFSET_PASSWORD + i] = pw[i];
                }
            }

        } else {
            return null;
        }

        return pairingFrame;
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    private ObisCode getClockObisCode() {
        return ObisCode.fromString("0.0.1.0.0.255");
    }

    @Override
    protected ProtocolConnection doInit(
            InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling,
            int protocolCompatible, Encryptor encryptor,
            HalfDuplexController halfDuplexController) throws IOException {

        getObjectEntries().put(getClockObisCode(), new ObjectEntry("Clock", 8));

        waveFlowConnect = new WaveFlowConnect(inputStream, outputStream, timeoutProperty, getLogger(), forcedDelay, getInfoTypeProtocolRetriesProperty());
        radioCommandFactory = new RadioCommandFactory(this);
        parameterFactory = new ParameterFactory(this);
        transparantObjectAccessFactory = new TransparantObjectAccessFactory(this);
        obisCodeMapper = new ObisCodeMapper(this);
        return waveFlowConnect;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec(CORRECTTIME.getName(), PropertyTranslationKeys.CORONIS_CORRECTTIME, false));
        propertySpecs.add(this.integerSpec("correctWaveflowTime", PropertyTranslationKeys.CORONIS_CORRECT_WAVEFLOW_TIME, false));
        propertySpecs.add(this.stringSpec("verifyProfileInterval", PropertyTranslationKeys.CORONIS_VERIFY_PROFILE_INTERVAL, false));
        propertySpecs.add(this.stringSpec("isOldFirmware", PropertyTranslationKeys.CORONIS_IS_OLD_FIRMWARE, false));
        propertySpecs.add(this.stringSpec("optimizeChangeContactorStatus", PropertyTranslationKeys.CORONIS_OPTIMIZE_CHANGE_CONTACTOR_STATUS, false));
        propertySpecs.add(this.stringSpec("SerialNumberA", PropertyTranslationKeys.CORONIS_SERIAL_NUMBER_A, false));
        propertySpecs.add(this.integerSpec("MaxNumberOfIntervals", PropertyTranslationKeys.CORONIS_MAX_NUMBER_OF_INTERVALS, false));
        propertySpecs.add(WaveflowProtocolUtils.propertySpec("WavenisEncryptionKey", false, getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_WAVENIS_ENCRYPTION_KEY).format(), getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_WAVENIS_ENCRYPTION_KEY_DESCRIPTION).format()));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "40000").trim()));
        correctTime = Integer.parseInt(properties.getTypedProperty(CORRECTTIME.getName(), "0"));
        correctWaveflowTime = Integer.parseInt(properties.getTypedProperty("correctWaveflowTime", "0"));
        verifyProfileInterval = Boolean.parseBoolean(properties.getTypedProperty("verifyProfileInterval", "false"));
        isOldFirmware = "1".equalsIgnoreCase(properties.getTypedProperty("isOldFirmware", "0"));
        optimizeChangeContactorStatus = "1".equalsIgnoreCase(properties.getTypedProperty("optimizeChangeContactorStatus", "0"));
        serialNumberA = properties.getTypedProperty("SerialNumberA", "");
        maxNumberOfIntervals = Integer.parseInt(properties.getTypedProperty("MaxNumberOfIntervals", "0"));

        String temp = properties.getTypedProperty("WavenisEncryptionKey");
        if (temp != null) {
            try {
                encryptor = new Encryption(WaveflowProtocolUtils.getArrayFromStringHexNotation(temp), getLogger());
            } catch (IOException e) {
                throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
            }
        }
    }

    public final boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "N/A";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
    public Date getTime() throws IOException {
        try {
            AbstractDataType o = transparantObjectAccessFactory.readObjectAttribute(getClockObisCode(), 2);
            return DateTimeFixer.getCorrectedDateTimeFromOctetString(o.getOctetString(), getTimeZone());
        } catch (WaveFlowExceptionNotPaired e) {
            return new Date();
        }
    }

    final void forceSetTime() throws IOException {
        DateTime dateTime = new DateTime(getTimeZone());
        transparantObjectAccessFactory.writeObjectAttribute(getClockObisCode(), 2, dateTime);
    }

    @Override
    public void setTime() throws IOException {
        if (correctTime > 0) {
            DateTime dateTime = new DateTime(getTimeZone());
            transparantObjectAccessFactory.writeObjectAttribute(getClockObisCode(), 2, dateTime);
            if (correctWaveflowTime > 0) {
                parameterFactory.writeTimeDateRTC(new Date());
            }
        }
    }

    void setWaveFlowTime() throws IOException {
        parameterFactory.writeTimeDateRTC(new Date());
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public void cacheRegisters(List<ObisCode> obisCodes) throws IOException {
        obisCodeMapper.cacheRegisters(obisCodes);
    }

    boolean pairWithEMeter(int baudrate) throws IOException {
        byte[] pairingframe = buildPairingFrame(baudrate);
        if (pairingframe == null) {
            getLogger().warning("Cannot pair with the meter again because password and/or meteraddress is not known... Wait 15 minutes for the waveflow to pair with the meter again...");
            return false;
        } else {
            int retry = 1;
            getLogger().warning("Try to pair with the meter, try [" + retry + "]...");

            while (true) {

                if (retry++ >= 5) {
                    getLogger().severe("Unable to pair with the meter after 5 retries, give up...");
                    return false;
                }

                byte[] pairingResponse = waveFlowConnect.sendData(pairingframe);
                // 30046E4AC000ABB002
                int PAIRING_RESULT_OFFFSET = 1;
                int PAIRING_RESULT_DATA_LENGTH = 2;
                int PAIRING_RESULT_DATA_OFFSET = 3;
                if (pairingResponse.length < 2) {
                    getLogger().warning("Pairing result length is invalid. Expected [9], received [" + pairingResponse.length + "], try [" + retry + "]...");
                } else {
                    if ((pairingResponse[PAIRING_RESULT_OFFFSET] > 0) && (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) < 0xFD)) {
                        int length = pairingResponse[PAIRING_RESULT_DATA_LENGTH];
                        byte[] data = ProtocolUtils.getSubArray(pairingResponse, PAIRING_RESULT_DATA_OFFSET);
                        getLogger().warning("Pairing with the meter was successfull, returned data is [" + ProtocolUtils.outputHexString(data) + "]");
                        return true;
                    } else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0) {
                        getLogger().warning("Pairing failed, no answer to GET Meter Serial Number, result code [0], leave loop!");
                        return false;
                    } else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0xFD) {
                        getLogger().warning("Pairing with the meter was already done, result code [0xFD], leave loop!");
                        return true;
                    } else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0xFE) {
                        getLogger().warning("Pairing failed, no meter connected or connection rejected, result code [0xFE], leave loop!");
                        return false;
                    } else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0xFF) {
                        getLogger().warning("Bad request format, result code [0xFF], leave loop!");
                        return false;
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e1);
                }

            }

        }
    }

    @Override
    public WaveFlowConnect getWaveFlowConnect() {
        return waveFlowConnect;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        waveFlowDLMSWMessages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveFlowDLMSWMessages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return waveFlowDLMSWMessages.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return waveFlowDLMSWMessages.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return waveFlowDLMSWMessages.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return waveFlowDLMSWMessages.writeValue(value);
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

    @Override
    public List map2MeterEvent(String event) throws IOException {
        //FIXME: we should implement a new interface in the protocols to be used for the alarm ack return data...
        AlarmFrameParser alarmFrame = new AlarmFrameParser(ProtocolUtils.convert2ascii(event.getBytes()), this);
        // this is tricky. We need to return the "alarmstatus" bytes to acknowledge the alarm.
        // so to avoid changing the signature of interfaceEventMapper in Ethernet, we add the return "alarmstatus" as first element in the list.
        List statusAndEvents = new ArrayList();
        statusAndEvents.add(alarmFrame.getResponseACK());
        statusAndEvents.add(alarmFrame.getMeterEvents());
        return statusAndEvents;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return EMETER_NR_OF_CHANNELS;
    }

    void renewEncryptionKey(String oldKey, String newKey) throws IOException {
        EncryptionKeyInitialization o = new EncryptionKeyInitialization(this);
        o.setKey(encryptor.encrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), WaveflowProtocolUtils.getArrayFromStringHexNotation(oldKey)));
        encryptor = new Encryption(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), getLogger());
        o.invoke();
    }

    void initializeEncryption(String newKey) throws IOException {
        EncryptionKeyInitialization o = new EncryptionKeyInitialization(this);
        byte[] initialKey = Encryption.generateEncryptedKey(waveFlowConnect.getEscapeCommandFactory().getRadioAddress());
        o.setKey(encryptor.encrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), initialKey));
        encryptor = new Encryption(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), getLogger());
        o.invoke();
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return obisCodeMapper.getRegisterExtendedLogging();
    }

    @Override
    public String getSerialNumber() {
        try {
            return readRegister(getUtilityIdObiscode()).getText().trim();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public int getInfoTypeProtocolRetriesProperty() {
        return super.getInfoTypeProtocolRetriesProperty();
    }

}