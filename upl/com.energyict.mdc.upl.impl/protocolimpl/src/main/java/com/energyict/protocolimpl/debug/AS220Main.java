package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.EventNumber;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import com.energyict.protocolimpl.dlms.as220.plc.PLCMessaging;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.util.*;

/**
 * @author jme
 */
public class AS220Main extends AbstractDebuggingMain<AS220> {

    private static final ObisCode DEVICE_ID1_OBISCODE = ObisCode.fromString("0.0.96.0.0.255");
    private static final ObisCode DEVICE_ID2_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode DEVICE_ID3_OBISCODE = ObisCode.fromString("0.0.96.2.0.255");
    private static final ObisCode DEVICE_ID4_OBISCODE = ObisCode.fromString("0.0.96.3.0.255");
    private static final ObisCode DEVICE_ID5_OBISCODE = ObisCode.fromString("0.0.96.4.0.255");

    private static final String DISCONNECT_EMETER = "<" + AS220Messaging.DISCONNECT_EMETER + ">1</" + AS220Messaging.DISCONNECT_EMETER + ">";
    private static final String CONNECT_EMETER = "<" + AS220Messaging.CONNECT_EMETER + ">1</" + AS220Messaging.CONNECT_EMETER + ">";
    private static final String ARM_EMETER = "<" + AS220Messaging.ARM_EMETER + ">1</" + AS220Messaging.ARM_EMETER + ">";

    private static final String RESCAN_PLCBUS = "<" + PLCMessaging.RESCAN_PLCBUS + ">1</" + PLCMessaging.RESCAN_PLCBUS + ">";
    private static final String FORCE_SET_CLOCK = "<" + AS220Messaging.FORCE_SET_CLOCK + ">1</" + AS220Messaging.FORCE_SET_CLOCK + ">";
    private static final String SET_PLC_TIMEOUTS1 = "<SetSFSKMacTimeouts SEARCH_INITIATOR_TIMEOUT=\"01\" SYNCHRONIZATION_CONFIRMATION_TIMEOUT=\"34\" TIME_OUT_NOT_ADDRESSED=\"56\" TIME_OUT_FRAME_NOT_OK=\"78\"> </SetSFSKMacTimeouts>";
    private static final String SET_PLC_TIMEOUTS2 = "<SetSFSKMacTimeouts SEARCH_INITIATOR_TIMEOUT=\"-\" SYNCHRONIZATION_CONFIRMATION_TIMEOUT=\"34\" TIME_OUT_NOT_ADDRESSED=\"56\" TIME_OUT_FRAME_NOT_OK=\"78\"> </SetSFSKMacTimeouts>";
    private static final String SET_PLC_TIMEOUTS3 = "<SetSFSKMacTimeouts SEARCH_INITIATOR_TIMEOUT=\"\" SYNCHRONIZATION_CONFIRMATION_TIMEOUT=\"34\" TIME_OUT_NOT_ADDRESSED=\"56\" TIME_OUT_FRAME_NOT_OK=\"78\"> </SetSFSKMacTimeouts>";
    private static final String SET_PLC_TIMEOUTS4 = "<SetSFSKMacTimeouts SYNCHRONIZATION_CONFIRMATION_TIMEOUT=\"34\" TIME_OUT_NOT_ADDRESSED=\"56\" TIME_OUT_FRAME_NOT_OK=\"78\"> </SetSFSKMacTimeouts>";

    private static final String SET_PLC_FREQUENCIES1 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"11\" CHANNEL1_FS=\"12\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES2 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"-\" CHANNEL1_FS=\"12\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES3 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"\" CHANNEL1_FS=\"12\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES4 = "<SetPlcChannelFrequencies CHANNEL1_FS=\"12\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES5 = "<SetPlcChannelFrequencies CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES6 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"11\" CHANNEL1_FS=\"-\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES7 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"11\" CHANNEL1_FS=\"\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES8 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"11\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES9 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"rr\" CHANNEL2_FM=\"21\" CHANNEL2_FS=\"22\" CHANNEL3_FM=\"31\" CHANNEL3_FS=\"32\" CHANNEL4_FM=\"41\" CHANNEL4_FS=\"42\" CHANNEL5_FM=\"51\" CHANNEL5_FS=\"52\" CHANNEL6_FM=\"61\" CHANNEL6_FS=\"62\"> </SetPlcChannelFrequencies>";
    private static final String SET_PLC_FREQUENCIES0 = "<SetPlcChannelFrequencies CHANNEL1_FM=\"72000\" CHANNEL1_FS=\"76800\" CHANNEL2_FM=\"81600\" CHANNEL2_FS=\"67200\" CHANNEL3_FM=\"86400\" CHANNEL3_FS=\"62400\" CHANNEL4_FM=\"91200\" CHANNEL4_FS=\"57600\" CHANNEL5_FM=\"52800\" CHANNEL5_FS=\"48000\" CHANNEL6_FM=\"43200\" CHANNEL6_FS=\"38400\"> </SetPlcChannelFrequencies>";

    private static final String SET_PLC_GAIN0 = "<SetSFSKGain MAX_RECEIVING_GAIN=\"0\" MAX_TRANSMITTING_GAIN=\"-\" SEARCH_INITIATOR_GAIN=\"-\"> </SetSFSKGain>";
    private static final String SET_PLC_GAIN1 = "<SetSFSKGain MAX_RECEIVING_GAIN=\"0\" MAX_TRANSMITTING_GAIN=\"0\" SEARCH_INITIATOR_GAIN=\"-\"> </SetSFSKGain>";
    private static final String SET_PLC_GAIN2 = "<SetSFSKGain MAX_RECEIVING_GAIN=\"0\" MAX_TRANSMITTING_GAIN=\"-\" SEARCH_INITIATOR_GAIN=\"6\"> </SetSFSKGain>";

    private static final String SET_PLC_REPEATER_0 = "<SetSFSKRepeater REPEATER=\"0\"> </SetSFSKRepeater>";
    private static final String SET_PLC_REPEATER_1 = "<SetSFSKRepeater REPEATER=\"1\"> </SetSFSKRepeater>";
    private static final String SET_PLC_REPEATER_2 = "<SetSFSKRepeater REPEATER=\"2\"> </SetSFSKRepeater>";
    private static final String SET_PLC_REPEATER_3 = "<SetSFSKRepeater REPEATER=\"3\"> </SetSFSKRepeater>";

    private static final String OBSERVER_FILENAME = "c:\\logging\\AS220Main\\communications.log";
    protected static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT+1");

    protected static final String COMPORT = "COM5";
    protected static final int BAUDRATE = 115200;
    protected static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    protected static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    protected static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;

    private static AS220 as220 = null;

    public AS220 getMeterProtocol() {
        if (as220 == null) {
            as220 = new AS220();
            log("Created new instance of " + as220.getClass().getCanonicalName() + " [" + as220.getProtocolVersion() + "]");
        }
        return as220;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        properties.setProperty("MaximumTimeDiff", "300");
        properties.setProperty("MinimumTimeDiff", "1");
        properties.setProperty("CorrectTime", "0");

        properties.setProperty("Retries", "5");
        properties.setProperty("Timeout", "20000");
        properties.setProperty("ForcedDelay", "200");

        properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_NONE);
        //properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_BOTH);
        properties.setProperty("ProfileInterval", "900");
        properties.setProperty("Password", "20100401");
        properties.setProperty("SerialNumber", "35021373");

        properties.setProperty("AddressingMode", "-1");
        properties.setProperty("Connection", "3");
        properties.setProperty("ClientMacAddress", "2");
        properties.setProperty("ServerLowerMacAddress", "1");
        properties.setProperty("ServerUpperMacAddress", "1");

        properties.setProperty("ProfileType", "0");

        properties.setProperty(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY, "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
        properties.setProperty(LocalSecurityProvider.DATATRANSPORTKEY, "000102030405060708090A0B0C0D0E0F");

        return properties;
    }

    private Properties getOpticalProperties() {
        Properties properties = getProperties();
        properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_NONE);
        properties.setProperty("AddressingMode", "2");
        properties.setProperty("Connection", "0");
        properties.setProperty("ClientMacAddress", "1");
        properties.setProperty("ServerLowerMacAddress", "17");
        properties.setProperty("ServerUpperMacAddress", "1");
        properties.setProperty("OpticalBaudrate", "5");
        return properties;
    }


    public ProfileData readProfile(boolean incluideEvents) throws IOException {
        Calendar from = Calendar.getInstance(DEFAULT_TIMEZONE);
        from.add(Calendar.SECOND, -1);
        ProfileData pd = getMeterProtocol().getProfileData(from.getTime(), incluideEvents);
        return pd;
    }

    public void readRegisters() {
        UniversalObject[] universalObjects = getMeterProtocol().getMeterConfig().getInstantiatedObjectList();
        for (UniversalObject uo : universalObjects) {
            if (uo.getClassID() == Register.CLASSID) {
                try {
                    log(getMeterProtocol().readRegister(uo.getObisCode()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void pulseContactor() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(DISCONNECT_EMETER, "1"));
        getMeterProtocol().queryMessage(new MessageEntry(ARM_EMETER, "2"));
        getMeterProtocol().queryMessage(new MessageEntry(CONNECT_EMETER, "3"));
    }

    public void rescanPLCBus() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(RESCAN_PLCBUS, ""));
    }

    public void setPLCTimeouts() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_TIMEOUTS1, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_TIMEOUTS2, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_TIMEOUTS3, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_TIMEOUTS4, ""));
    }

    public void setPLCFrequencies() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES1, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES2, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES3, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES4, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES5, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES6, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES7, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES8, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES9, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_FREQUENCIES0, ""));
    }

    public void setPLCGain() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_GAIN0, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_GAIN1, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_GAIN2, ""));
    }

    public void setPLCRepeater() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_REPEATER_0, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_REPEATER_1, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_REPEATER_2, ""));
        getMeterProtocol().queryMessage(new MessageEntry(SET_PLC_REPEATER_3, ""));
    }

    public void forceSetClock() throws IOException {
        getMeterProtocol().queryMessage(new MessageEntry(FORCE_SET_CLOCK, ""));
    }

    public void readObjectList() throws IOException {
        UniversalObject[] uo = getMeterProtocol().getMeterConfig().getInstantiatedObjectList();
        for (UniversalObject universalObject : uo) {
            System.out.println(universalObject.getObisCode() + " = " + DLMSClassId.findById(universalObject.getClassID()) + " [" + universalObject.getBaseName() + "] " + universalObject.getObisCode().getDescription());
        }
    }

    public void readDataObjects() {
        UniversalObject[] uo = getMeterProtocol().getMeterConfig().getInstantiatedObjectList();
        for (UniversalObject universalObject : uo) {
            if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
                try {
                    log(universalObject.getObisCode() + " = " + getMeterProtocol().getCosemObjectFactory().getData(universalObject.getObisCode()));
                } catch (IOException e) {
                    //Absorb
                }
            }
        }
    }

    public void getAndSetTime() throws IOException {
        Date date = getMeterProtocol().getTime();
        log(date);
        getMeterProtocol().setTime();
        date = getMeterProtocol().getTime();
        log(date);
    }

    /**
     * @throws IOException
     */
    private void readContactorStatus() throws IOException {
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.96.3.10.1")));
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.96.3.10.2")));
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.96.3.10.3")));
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.96.3.10.4")));
    }

    /**
     * @throws IOException
     */
    private void dumpEvents() throws IOException {
        Array a = new Array(getMeterProtocol().getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.0.255")).getBufferData(), 0, 0);
        for (int i = 0; i < a.nrOfDataTypes(); i++) {
            Date date = a.getDataType(i).getStructure().getDataType(0).getOctetString().getDateTime(DEFAULT_TIMEZONE).getValue().getTime();
            int value = a.getDataType(i).getStructure().getDataType(1).getTypeEnum().getValue();

            System.out.println(date + " = " + value + ",  " + EventNumber.toMeterEvent(value, date));
        }
    }

    public void readEnergyRegisters() {
        String[] registers = new String[]{
                "1.0.1.8.0.",
                "1.0.1.8.1.",
                "1.0.1.8.2.",
                "1.0.1.8.3.",
                "1.0.1.8.4.",
                "1.0.2.8.0.",
                "1.0.2.8.1.",
                "1.0.2.8.2.",
                "1.0.2.8.3.",
                "1.0.2.8.4."
        };

        for (int i = 0; i < registers.length; i++) {
            readRegister(registers[i] + "255");
            readRegister(registers[i] + "VZ");
            readRegister(registers[i] + "VZ-1");
            readRegister(registers[i] + "VZ-2");
            readRegister(registers[i] + "VZ-3");
            readRegister(registers[i] + "VZ-4");
        }
    }

    /**
     *
     */
    private void readMappedAttributes(List<ObisCode> codes) {
        for (Iterator iterator = codes.iterator(); iterator.hasNext();) {
            ObisCode code = (ObisCode) iterator.next();
            for (int i = 0; i <= 20; i++) {
                try {
                    ObisCode obis = ProtocolTools.setObisCodeField(code, 5, (byte) i);
                    log(obis.toString() + " " + getMeterProtocol().translateRegister(obis) + " = " + getMeterProtocol().readRegister(obis).getText());
                } catch (Exception e) {
                }
            }
            log("");
        }
    }

    /**
     * @throws IOException
     */
    private void readSFSKObjects() throws IOException {
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.26.0.0.255")) + "\r\n");
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.26.1.0.255")) + "\r\n");
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.26.2.0.255")) + "\r\n");
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.26.3.0.255")) + "\r\n");
        log(getMeterProtocol().readRegister(ObisCode.fromString("0.0.26.5.0.255")) + "\r\n");
    }

    private void examineObisCode(ObisCode obisCode) {
        log("");
        log(obisCode + " = " + obisCode.getDescription());
        for (int i = 0; i < 0x70; i += 8) {
            try {
                GenericRead gr = getMeterProtocol().getCosemObjectFactory().getGenericRead(obisCode, i);
                AbstractDataType dataType = AXDRDecoder.decode(gr.getResponseData());
                String value = ProtocolTools.getHexStringFromBytes(gr.getResponseData());
                log(i + " = " + dataType.getClass().getSimpleName() + " " + value);
            } catch (IOException e) {

            }
        }
        log("");
    }

    private byte[] getFirmware18ByteArray() throws IOException {
        File file = new File(AS220Main.class.getClassLoader().getResource("com/energyict/protocolimpl/dlms/as220/debug/firmware18b64.bin").getFile());
        FileInputStream fis = new FileInputStream(file);
        byte[] content = new byte[(int) file.length()];
        fis.read(content);
        fis.close();
        return content;
    }

    public void printExtendedLogging() throws IOException {
        log(getMeterProtocol().getRegistersInfo());
    }

    private byte[] getFirmware19ByteArray() throws IOException {
        File file = new File(AS220Main.class.getClassLoader().getResource("com/energyict/protocolimpl/dlms/as220/debug/firmware17022010B64.bin").getFile());
        FileInputStream fis = new FileInputStream(file);
        byte[] content = new byte[(int) file.length()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static void main(String[] args) throws LinkException, IOException, InterruptedException {
        AS220Main main = new AS220Main();
        main.setCommPort(COMPORT);
        main.setBaudRate(BAUDRATE);
        main.setDataBits(DATABITS);
        main.setParity(PARITY);
        main.setStopBits(STOPBITS);
        main.setObserverFilename(OBSERVER_FILENAME);
        main.run();
    }

    @Override
    void doDebug() throws LinkException, IOException {
        setPLCRepeater();
        readRegister("0.0.26.0.0.10");
    }

}
