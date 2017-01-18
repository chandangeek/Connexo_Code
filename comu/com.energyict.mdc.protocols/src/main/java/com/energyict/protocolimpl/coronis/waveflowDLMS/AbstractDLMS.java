package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.RegisterCache;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocols.util.EventMapper;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public abstract class AbstractDLMS extends AbstractProtocol implements ProtocolLink, MessageProtocol, EventMapper, RegisterCache {

    protected enum PairingMeterId {
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

    public AbstractDLMS(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /**
     * Validates if the serial number matches the utility ID (some kind of extended serial number) of the connected e-meter
     */
    @Override
    protected void doConnect() throws IOException {
        if (serialNumberA != null && !serialNumberA.isEmpty()) {
            String meterSerialNumber;
            try {
                meterSerialNumber = readRegister(getUtilityIdObiscode()).getText().trim();
            } catch (WaveFlowDLMSException e) {
                getLogger().warning("Error while reading out the meter serial number: " + e.getMessage());
                String msg = "Expected a meter with serial number '" + serialNumberA + "', but no meter is connected!";
                getLogger().warning(msg);
                IOException e1 = new IOException(msg);
                throw new NestedIOException(e1);     //To get completion code 2 (IO Error) instead of code 3 (protocol error)
            }
            if (!serialNumberA.equalsIgnoreCase(meterSerialNumber)) {
                String msg = "Serial number mismatch. Configured serial number for connected meter is '" + serialNumberA + "', while the actual serial number is '" + meterSerialNumber + "'";
                getLogger().warning(msg);
                IOException e = new IOException(msg);
                throw new NestedIOException(e);     //To get completion code 2 (IO Error) instead of code 3 (protocol error)
            }
        }
    }

    private static final int EMETER_NR_OF_CHANNELS = 4;
    protected static final String PROPERTY_LP_MULTIPLIER = "ApplyLoadProfileMultiplier";

    protected abstract void doTheValidateProperties(Properties properties);

    protected abstract ObisCode getSerialNumberObisCodeForPairingRequest();

    protected abstract ObisCode getUtilityIdObiscode();

    protected abstract Map<ObisCode, ObjectEntry> getObjectEntries();

    protected abstract PairingMeterId getPairingMeterId();

    public ObjectEntry findObjectByObiscode(final ObisCode obisCode) throws NoSuchRegisterException {
        ObjectEntry o = getObjectEntries().get(obisCode);
        if (o == null) {
            throw new NoSuchRegisterException("Register with obiscode [" + obisCode + "] not found.");
        } else {
            return o;
        }
    }

    public boolean isOptimizeChangeContactorStatus() {
        return optimizeChangeContactorStatus;
    }

    public Entry<ObisCode, ObjectEntry> findEntryByDescription(final String description) throws NoSuchRegisterException {

        for (Entry<ObisCode, ObjectEntry> o : getObjectEntries().entrySet()) {
            if (o.getValue().getDescription().compareTo(description) == 0) {
                return o;
            }
        }

        throw new NoSuchRegisterException("Register with description [" + description + "] not found.");
    }

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

    public int getMaxNumberOfIntervals() {
        return maxNumberOfIntervals;
    }

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
     * Check if the AM700 board has old (1.0.X or lower) firmware
     * This is important for the parsing of the event code
     */
    public boolean isOldFirmware() {
        return isOldFirmware;
    }

    /**
     * Indicates whether or not to use the simple contactor change method
     */
    private boolean optimizeChangeContactorStatus = false;

    /**
     * Reference to the parameter factory
     */
    private ParameterFactory parameterFactory;

    public final ParameterFactory getParameterFactory() {
        return parameterFactory;
    }

    public final RadioCommandFactory getRadioCommandFactory() {
        return radioCommandFactory;
    }

    public final TransparantObjectAccessFactory getTransparantObjectAccessFactory() {
        return transparantObjectAccessFactory;
    }

    /**
     * the encryptor for the data
     */
    Encryption encryptor = new Encryption(null, getLogger());

    final Encryption getEncryptor() {
        return encryptor;
    }

    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        // absorb
    }

    /**
     * reference to the lower connect latyers of the wavenis stack
     */
    private WaveFlowConnect waveFlowConnect;


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
    protected void doDisConnect() throws IOException {
    }

    @Override
    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        list.add("correctWaveflowTime");
        list.add("verifyProfileInterval");
        list.add("LoadProfileObisCode");
        list.add("WavenisEncryptionKey");
        list.add("isOldFirmware");
        list.add("optimizeChangeContactorStatus");
        list.add("SerialNumberA");
        list.add("MaxNumberOfIntervals");
        list.add(PROPERTY_LP_MULTIPLIER);
        return list;
    }

    protected ObisCode getClockObisCode() {
        return ObisCode.fromString("0.0.1.0.0.255");
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling,
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
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout", "40000").trim()));
        correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME, "0"));
        correctWaveflowTime = Integer.parseInt(properties.getProperty("correctWaveflowTime", "0"));
        verifyProfileInterval = Boolean.parseBoolean(properties.getProperty("verifyProfileInterval", "false"));
        isOldFirmware = "1".equalsIgnoreCase(properties.getProperty("isOldFirmware", "0"));
        optimizeChangeContactorStatus = "1".equalsIgnoreCase(properties.getProperty("optimizeChangeContactorStatus", "0"));
        serialNumberA = properties.getProperty("SerialNumberA", "");
        maxNumberOfIntervals = Integer.parseInt(properties.getProperty("MaxNumberOfIntervals", "0"));

        String temp = properties.getProperty("WavenisEncryptionKey");
        if (temp != null) {
            try {
                encryptor = new Encryption(WaveflowProtocolUtils.getArrayFromStringHexNotation(temp), getLogger());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                throw new InvalidPropertyException("WavenisEncryptionKey invalid [" + temp + "]");
            }
        }


        doTheValidateProperties(properties);
    }

    public final boolean isVerifyProfileInterval() {
        return verifyProfileInterval;
    }

    @Override
    public String getFirmwareVersion() throws IOException {

        return "N/A";

//		try {
//			return "V"+WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion())+", Mode of transmission "+getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
//		} catch (IOException e) {
//			return "Error requesting firmware version";
//		}
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-06-10 10:15:31 +0200 (Mon, 10 Jun 2013) $";
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

    public void setWaveFlowTime() throws IOException {
        parameterFactory.writeTimeDateRTC(new Date());
    }


    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     *
     * @param obisCode obiscode of the register to lookup
     * @return RegisterInfo object
     * @throws java.io.IOException thrown when somethiong goes wrong
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public void cacheRegisters(List<ObisCode> obisCodes) throws IOException {
        obisCodeMapper.cacheRegisters(obisCodes);
    }

    public boolean pairWithEMeter(int baudrate) throws IOException {

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
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            } // while(true)

        } // else
    }

    public WaveFlowConnect getWaveFlowConnect() {
        return waveFlowConnect;
    }

    public void applyMessages(List messageEntries) throws IOException {
        waveFlowDLMSWMessages.applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return waveFlowDLMSWMessages.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return waveFlowDLMSWMessages.getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return waveFlowDLMSWMessages.writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return waveFlowDLMSWMessages.writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return waveFlowDLMSWMessages.writeValue(value);
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

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

    public int getNumberOfChannels() throws IOException {
        return EMETER_NR_OF_CHANNELS;
    }

    public EncryptionKeyInitialization readEncryptionKeyInitialization() throws IOException {
        EncryptionKeyInitialization o = new EncryptionKeyInitialization(this);
        o.invoke();
        return o;
    }

    public void renewEncryptionKey(String oldKey, String newKey) throws IOException {
        EncryptionKeyInitialization o = new EncryptionKeyInitialization(this);
        o.setKey(encryptor.encrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), WaveflowProtocolUtils.getArrayFromStringHexNotation(oldKey)));
        encryptor = new Encryption(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), getLogger());
        o.invoke();
    }

    public void initializeEncryption(String newKey) throws IOException {
        EncryptionKeyInitialization o = new EncryptionKeyInitialization(this);
        byte[] initialKey = Encryption.generateEncryptedKey(waveFlowConnect.getEscapeCommandFactory().getRadioAddress());
        o.setKey(encryptor.encrypt(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), initialKey));
        encryptor = new Encryption(WaveflowProtocolUtils.getArrayFromStringHexNotation(newKey), getLogger());
        o.invoke();
    }

    /**
     * Override if you want to provide info of the meter setup and registers when the "ExtendedLogging" custom property > 0
     *
     * @param extendedLogging int
     * @return String with info
     * @throws java.io.IOException thrown when something goes wrong
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return obisCodeMapper.getRegisterExtendedLogging();
    }

}
