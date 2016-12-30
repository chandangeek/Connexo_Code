package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityPolicy;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.EventNumber;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import com.energyict.protocolimpl.dlms.as220.plc.PLCMessaging;
import com.energyict.protocolimpl.dlms.as220.plc.events.PLCLog;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author jme
 */
public class AS220Main extends AbstractDebuggingMain<AS220> {

    protected static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
    protected static final int BAUDRATE = 115200;
    protected static final int DATABITS = SerialCommunicationChannel.DATABITS_8;
    protected static final int PARITY = SerialCommunicationChannel.PARITY_NONE;
    protected static final int STOPBITS = SerialCommunicationChannel.STOPBITS_1;
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
    private static final String SET_PLC_SNR_CREDIT_FREQ0 = "<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"76800\" CHANNEL1_FM=\"72000\" CHANNEL1_SNR=\"1.7\" CHANNEL1_CREDITWEIGHT=\"0.2\" CHANNEL2_FS=\"81600\" CHANNEL2_FM=\"67200\" CHANNEL2_SNR=\"1.0\" CHANNEL2_CREDITWEIGHT=\"1.0\" CHANNEL3_FS=\"86400\" CHANNEL3_FM=\"62400\" CHANNEL3_SNR=\"1.0\" CHANNEL3_CREDITWEIGHT=\"1.0\" CHANNEL4_FS=\"91200\" CHANNEL4_FM=\"57600\" CHANNEL4_SNR=\"1.0\" CHANNEL4_CREDITWEIGHT=\"1.0\" CHANNEL5_FS=\"52800\" CHANNEL5_FM=\"48000\" CHANNEL5_SNR=\"1.0\" CHANNEL5_CREDITWEIGHT=\"1.0\" CHANNEL6_FS=\"43200\" CHANNEL6_FM=\"38400\" CHANNEL6_SNR=\"1.0\" CHANNEL6_CREDITWEIGHT=\"1.0\"> </SetPlcChannelFrequenciesSnrCredits>";
    private static final String SET_PLC_SNR_CREDIT_FREQ1 = "<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"76800\" CHANNEL1_FM=\"72000\" CHANNEL1_SNR=\"2.6\" CHANNEL1_CREDITWEIGHT=\"0.4\" CHANNEL3_FS=\"86400\" CHANNEL3_FM=\"62400\" CHANNEL3_SNR=\"1.0\" CHANNEL3_CREDITWEIGHT=\"1.0\" CHANNEL4_FS=\"91200\" CHANNEL4_FM=\"57600\" CHANNEL4_SNR=\"1.0\" CHANNEL4_CREDITWEIGHT=\"1.0\" CHANNEL5_FS=\"52800\" CHANNEL5_FM=\"48000\" CHANNEL5_SNR=\"1.0\" CHANNEL5_CREDITWEIGHT=\"1.0\" CHANNEL6_FS=\"43200\" CHANNEL6_FM=\"38400\" CHANNEL6_SNR=\"1.0\" CHANNEL6_CREDITWEIGHT=\"1.0\"> </SetPlcChannelFrequenciesSnrCredits>";
    private static final String SET_PLC_SNR_CREDIT_FREQ2 = "<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"76800\" CHANNEL1_FM=\"72000\" CHANNEL1_SNR=\"3.5\" CHANNEL1_CREDITWEIGHT=\"0.6\" CHANNEL4_FS=\"91200\" CHANNEL4_FM=\"57600\" CHANNEL4_SNR=\"1.0\" CHANNEL4_CREDITWEIGHT=\"1.0\" CHANNEL5_FS=\"52800\" CHANNEL5_FM=\"48000\" CHANNEL5_SNR=\"1.0\" CHANNEL5_CREDITWEIGHT=\"1.0\" CHANNEL6_FS=\"43200\" CHANNEL6_FM=\"38400\" CHANNEL6_SNR=\"1.0\" CHANNEL6_CREDITWEIGHT=\"1.0\"> </SetPlcChannelFrequenciesSnrCredits>";
    private static final String SET_PLC_SNR_CREDIT_FREQ3 = "<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"76800\" CHANNEL1_FM=\"72000\" CHANNEL1_SNR=\"4.4\" CHANNEL1_CREDITWEIGHT=\"0.8\" CHANNEL5_FS=\"52800\" CHANNEL5_FM=\"48000\" CHANNEL5_SNR=\"1.0\" CHANNEL5_CREDITWEIGHT=\"1.0\" CHANNEL6_FS=\"43200\" CHANNEL6_FM=\"38400\" CHANNEL6_SNR=\"1.0\" CHANNEL6_CREDITWEIGHT=\"1.0\"> </SetPlcChannelFrequenciesSnrCredits>";
    private static final String SET_PLC_SNR_CREDIT_FREQ4 = "<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"76800\" CHANNEL1_FM=\"72000\" CHANNEL1_SNR=\"5.3\" CHANNEL1_CREDITWEIGHT=\"0.0\" CHANNEL6_FS=\"43200\" CHANNEL6_FM=\"38400\" CHANNEL6_SNR=\"1.0\" CHANNEL6_CREDITWEIGHT=\"1.0\"> </SetPlcChannelFrequenciesSnrCredits>";
    private static final String SET_PLC_SNR_CREDIT_FREQ5 = "<SetPlcChannelFreqSnrCredits CHANNEL1_FS=\"76800\" CHANNEL1_FM=\"72000\" CHANNEL1_SNR=\"6.2\" CHANNEL1_CREDITWEIGHT=\"0.1\"> </SetPlcChannelFrequenciesSnrCredits>";
    private static final String SET_PLC_GAIN0 = "<SetSFSKGain MAX_RECEIVING_GAIN=\"0\" MAX_TRANSMITTING_GAIN=\"-\" SEARCH_INITIATOR_GAIN=\"-\"> </SetSFSKGain>";
    private static final String SET_PLC_GAIN1 = "<SetSFSKGain MAX_RECEIVING_GAIN=\"0\" MAX_TRANSMITTING_GAIN=\"0\" SEARCH_INITIATOR_GAIN=\"-\"> </SetSFSKGain>";
    private static final String SET_PLC_GAIN2 = "<SetSFSKGain MAX_RECEIVING_GAIN=\"0\" MAX_TRANSMITTING_GAIN=\"-\" SEARCH_INITIATOR_GAIN=\"6\"> </SetSFSKGain>";
    private static final String SET_PLC_REPEATER_0 = "<SetSFSKRepeater REPEATER=\"0\"> </SetSFSKRepeater>";
    private static final String SET_PLC_REPEATER_1 = "<SetSFSKRepeater REPEATER=\"1\"> </SetSFSKRepeater>";
    private static final String SET_PLC_REPEATER_2 = "<SetSFSKRepeater REPEATER=\"2\"> </SetSFSKRepeater>";
    private static final String SET_PLC_REPEATER_3 = "<SetSFSKRepeater REPEATER=\"3\"> </SetSFSKRepeater>";
    private static final String WRITE_IEC_DATA_0 = "<WriteRawIEC1107Class IEC1107ClassId=\"2\" Offset=\"0\" RawData=\"26000000990F0000000C0000000000004803233800400000000000000000005550000000000000000300000000000000000000000000DD0000000000073300000000009903040000000000000000000000000000000000000000000000000000007800000000000000000068\"> </WriteRawIEC1107Class>";
    private static final String WRITE_IEC_DATA_5 = "<WriteRawIEC1107Class IEC1107ClassId=\"2\" Offset=\"0\" RawData=\"26000000990F0000000C0000000000004803233800400000000000000000005550000000000000000300000000000000000000000000DD0000000000073300000000009903040000000000000000000000000000000000000000000000000000007800140000000000000054\"> </WriteRawIEC1107Class>";
    private static final String FIRMWARE_UPGRADE = "<FirmwareUpdate><IncludedFile>$CONTENT$</IncludedFile></FirmwareUpdate>";
    private static final String ACTIVITY_CALENDAR = "<TimeOfUse>$CONTENT$</TimeOfUse>";
    private static final String ACTIVATE_PASSIVE_CALENDAR = "<ActivatePassiveCalendar ActivationTime=\"$ACT_DATE$\"> </ActivatePassiveCalendar>";
    private static final String LOADLIMIT_DURATION_MSG = "<SetLoadLimitDuration LoadLimitDuration=\"$DURATION$\"> </SetLoadLimitDuration>";
    private static final String LOADLIMIT_THRESHOLD_MSG = "<SetLoadLimitThreshold LoadLimitThreshold=\"$THRESHOLD$\"> </SetLoadLimitThreshold>";
    private static final String OBSERVER_FILENAME = "c:\\logging\\AS220Main\\communications_"+System.currentTimeMillis()+".log";
    private static final boolean AS1440 = true;
    protected static final String COMPORT = AS1440 ? "COM5" : "COM7";
    private static AS220 as220 = null;

    public static void main(String[] args) throws LinkException, IOException, InterruptedException {
        AS220Main main = new AS220Main();
        main.setCommPort(COMPORT);
        main.setBaudRate(BAUDRATE);
        main.setDataBits(DATABITS);
        main.setParity(PARITY);
        main.setStopBits(STOPBITS);
        main.setObserverFilename(null);
        main.setShowCommunication(false);
        main.setTimeZone(DEFAULT_TIMEZONE);
        main.run();
    }

    public AS220 getMeterProtocol() {
        if (as220 == null) {
            as220 = new AS220(Services.propertySpecService(), new NoTariffCalendars(), new DummyNumberLookupExtractor());
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

        properties.setProperty("SecurityLevel", "1:" + SecurityPolicy.SECURITYPOLICY_BOTH);
        properties.setProperty("ProfileInterval", "900");
        properties.setProperty("Password", "20100401");
        properties.setProperty("SerialNumber", AS1440 ? "03191576" : "35021373");

        properties.setProperty("AddressingMode", "-1");
        properties.setProperty("Connection", "3");
        properties.setProperty("ClientMacAddress", "2");
        properties.setProperty("ServerLowerMacAddress", "1");
        properties.setProperty("ServerUpperMacAddress", "1");

        properties.setProperty("ProfileType", "1");

        properties.setProperty("LimitMaxNrOfDays", "0");

        properties.setProperty(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY, "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
        properties.setProperty(LocalSecurityProvider.DATATRANSPORTKEY, "000102030405060708090A0B0C0D0E0F");

        return properties;
    }

    private Properties getOpticalProperties() {
        Properties properties = getProperties();
        properties.setProperty("SecurityLevel", "1:" + SecurityPolicy.SECURITYPOLICY_NONE);
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
        from.add(Calendar.MONTH, -1);
        ProfileData pd = getMeterProtocol().getProfileData(from.getTime(), incluideEvents);
        return pd;
    }

    public ProfileData readProfileFrom(Calendar from, boolean includeEvents) throws IOException {
        return getMeterProtocol().getProfileData(from.getTime(), includeEvents);
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
        getMeterProtocol().queryMessage(MessageEntry.fromContent(DISCONNECT_EMETER).trackingId("1").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(ARM_EMETER).trackingId("2").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(CONNECT_EMETER).trackingId("3").finish());
    }

    public void rescanPLCBus() throws IOException {
        getMeterProtocol().queryMessage(MessageEntry.fromContent(RESCAN_PLCBUS).trackingId("").finish());
    }

    public void setPLCTimeouts() throws IOException {
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_TIMEOUTS1).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_TIMEOUTS2).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_TIMEOUTS3).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_TIMEOUTS4).trackingId("").finish());
    }

    public void setPLCFrequencies() throws IOException {
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES1).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES2).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES3).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES4).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES5).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES6).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES7).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES8).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES9).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_FREQUENCIES0).trackingId("").finish());
    }

    public void setPLCFreqSnrCredits() throws IOException {
        if (AS1440) {
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ0).trackingId("").finish());
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ1).trackingId("").finish());
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ2).trackingId("").finish());
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ3).trackingId("").finish());
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ4).trackingId("").finish());
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ5).trackingId("").finish());
            getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_SNR_CREDIT_FREQ0).trackingId("").finish());
        }
    }

    public void setPLCGain() throws IOException {
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_GAIN0).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_GAIN1).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_GAIN2).trackingId("").finish());
    }

    public void setPLCRepeater() throws IOException {
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_REPEATER_0).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_REPEATER_1).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_REPEATER_2).trackingId("").finish());
        getMeterProtocol().queryMessage(MessageEntry.fromContent(SET_PLC_REPEATER_3).trackingId("").finish());
    }

    public void forceSetClock() throws IOException {
        getMeterProtocol().queryMessage(MessageEntry.fromContent(FORCE_SET_CLOCK).trackingId("").finish());
    }

    public void readObjectList() throws IOException {
        UniversalObject[] uo = getMeterProtocol().getMeterConfig().getInstantiatedObjectList();
        for (UniversalObject universalObject : uo) {
            System.out.println(universalObject.getObisCode() + " = " + DLMSClassId.findById(universalObject.getClassID()) + " [" + universalObject.getBaseName() + "] " + universalObject.getObisCode().toString());
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
        log(obisCode + " = " + obisCode.toString());
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

    public void printExtendedLogging() throws IOException {
        log(getMeterProtocol().getRegistersInfo());
    }

    private List<String> getFullRegisterList() {
        List<String> registers = new ArrayList<>();

        registers.add("0.0.26.0.0.10");
        registers.add("0.0.26.0.0.11");
        registers.add("0.0.26.0.0.12");
        registers.add("0.0.26.0.0.13");
        registers.add("0.0.26.0.0.14");
        registers.add("0.0.26.0.0.16");

        registers.add("0.0.26.1.0.1");
        registers.add("0.0.26.1.0.2");

        registers.add("0.0.26.2.0.1");
        registers.add("0.0.26.2.0.2");
        registers.add("0.0.26.2.0.3");
        registers.add("0.0.26.2.0.4");
        registers.add("0.0.26.2.0.5");

        registers.add("0.0.26.3.0.1");
        registers.add("0.0.26.3.0.2");
        registers.add("0.0.26.3.0.3");
        registers.add("0.0.26.3.0.4");
        registers.add("0.0.26.3.0.5");
        registers.add("0.0.26.3.0.6");
        registers.add("0.0.26.3.0.7");
        registers.add("0.0.26.3.0.8");

        registers.add("0.0.26.5.0.1");
        registers.add("0.0.26.5.0.2");
        registers.add("0.0.26.5.0.3");

        registers.add("0.0.96.3.10.1");
        registers.add("0.0.96.3.10.2");
        registers.add("0.0.96.3.10.3");
        registers.add("0.0.96.3.10.4");

        registers.add("1.0.31.7.0.255");
        registers.add("1.0.32.7.0.255");
        registers.add("1.0.9.7.0.255");
        registers.add("1.0.29.7.0.255");

        registers.add("1.0.1.8.1.255");
        registers.add("1.0.1.8.2.255");
        registers.add("1.0.1.8.3.255");
        registers.add("1.0.1.8.4.255");
        registers.add("1.0.1.8.0.255");
        registers.add("1.0.2.8.0.255");

        registers.add("1.0.1.8.1.255");
        registers.add("1.0.1.8.2.255");
        registers.add("1.0.2.8.1.255");
        registers.add("1.0.2.8.2.255");

        registers.add("1.0.1.8.1.VZ");
        registers.add("1.0.1.8.2.VZ");
        registers.add("1.0.2.8.1.VZ");
        registers.add("1.0.2.8.2.VZ");

        registers.add("1.0.1.8.1.VZ-1");
        registers.add("1.0.1.8.2.VZ-1");
        registers.add("1.0.2.8.1.VZ-1");
        registers.add("1.0.2.8.2.VZ-1");

        registers.add("1.0.1.8.1.VZ-2");
        registers.add("1.0.1.8.2.VZ-2");
        registers.add("1.0.2.8.1.VZ-2");
        registers.add("1.0.2.8.2.VZ-2");

        registers.add("1.0.1.8.1.VZ-3");
        registers.add("1.0.1.8.2.VZ-3");
        registers.add("1.0.2.8.1.VZ-3");
        registers.add("1.0.2.8.2.VZ-3");

        registers.add("1.0.1.8.1.VZ-4");
        registers.add("1.0.1.8.2.VZ-4");
        registers.add("1.0.2.8.1.VZ-4");
        registers.add("1.0.2.8.2.VZ-4");

        registers.add("0.0.96.1.0.255");
        registers.add("0.0.96.14.0.255");

        registers.add("1.0.0.2.0.255");

        return registers;

    }

    private void readRegisters(List registers) {
        for (Object register : registers) {
            ObisCode obis;
            if (register instanceof String) {
                obis = ObisCode.fromString((String) register);
            } else if (register instanceof ObisCode) {
                obis = (ObisCode) register;
            } else {
                obis = null;
            }

            if (obis != null) {
                try {
                    RegisterValue registerValue = getMeterProtocol().readRegister(obis);
                    System.out.println(registerValue);
                } catch (IOException e) {
                    System.out.println("ERROR: [" + register + "]: " + e.getMessage());
                }
            }

        }
    }

    private void readRegisters(String... registers) {
        readRegisters(Arrays.asList(registers));
    }

    private void readRegisters(ObisCode... registers) {
        readRegisters(Arrays.asList(registers));
    }

    public void firmwareUpgrade(byte[] base64Firmware) throws IOException {
        System.out.println("Firmware before upgrade: " + getMeterProtocol().getFirmwareVersion());
        String message = FIRMWARE_UPGRADE.replace("$CONTENT$", new String(base64Firmware));
        MessageResult result = getMeterProtocol().queryMessage(MessageEntry.fromContent(message).trackingId("").finish());
        System.out.println("Firmware upgrade " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
        System.out.println("Firmware after upgrade: " + getMeterProtocol().getFirmwareVersion());
    }

    private String getB64EncodedFirmareString() throws IOException {
        File file = new File("C:\\Documents and Settings\\jme\\Desktop\\AM500_20110607_V2.08\\MeterEandis.v2.08_Serial_Release_ImageTransfer.bin");
        byte[] content = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Base64EncoderDecoder base64Encoder = new Base64EncoderDecoder();
        return base64Encoder.encode(content);
    }

    public void activityCalendarUpgrade(String xmlContent) throws IOException {
        MessageResult result = getMeterProtocol().queryMessage(MessageEntry.fromContent(xmlContent).trackingId("trackGna").finish());
        System.out.println("ActivityCalender upgrade " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
    }

    public void readAllCalendarObjects(ActivityCalendar ac) throws IOException {
        log("Active Calendar Name : ");
       log(ac.readCalendarNameActive().stringValue());

        log("Passive Calendar Name : ");
        log(ac.readCalendarNamePassive().stringValue());

        log("ActivatePassiveCalendar at : ");
        log(ParseUtils.decimalByteToString(ac.readActivatePassiveCalendarTime().getBEREncodedByteArray()));

        log("SeasonProfileActive : ");
        log(ac.readSeasonProfileActive());

        log("SeasonProfilePassive : ");
        log(ac.readSeasonProfilePassive());

        log("WeekProfileActive : ");
        log(ac.readWeekProfileTableActive());

        log("WeekProfilePassive : ");
        log(ac.readWeekProfileTablePassive());

        log("DayProfileActive : ");
        log(ac.readDayProfileTableActive());

        log("DayProfilePassive : ");
        log(ac.readDayProfileTablePassive());

        log("SpecialDays : ");
        SpecialDaysTable sdt = getMeterProtocol().getCosemObjectFactory().getSpecialDaysTable(getMeterProtocol().getMeterConfig().getSpecialDaysTable().getObisCode());
        log(sdt.readSpecialDays());
    }

    public void writePassiveActivityCalendarTime(String gmtTime) throws IOException {
        String message = ACTIVATE_PASSIVE_CALENDAR.replace("$ACT_DATE$", gmtTime);
        MessageResult result = getMeterProtocol().queryMessage(MessageEntry.fromContent(message).trackingId("").finish());
        System.out.println("Activate passive Calendar : " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
    }

    public void readLoadLimitParameters(Limiter loadLimiter) throws IOException {
        log("Monitored value : " + loadLimiter.readMonitoredValue().getLogicalName());
        log("Threshold normal : " + loadLimiter.readThresholdNormal());
        log("Threshold duration : " + loadLimiter.readMinOverThresholdDuration());
    }

    public void writeLoadLimitThreshold(String threshold) throws IOException {
        String message = LOADLIMIT_THRESHOLD_MSG.replace("$THRESHOLD$", threshold);
        MessageResult result = getMeterProtocol().queryMessage(MessageEntry.fromContent(message).trackingId("").finish());
        System.out.println("Write LoadLimit threshold : " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
    }

    public void writeLoadLimitDuration(String duration) throws IOException {
        String message = LOADLIMIT_DURATION_MSG.replace("$DURATION$", duration);
        MessageResult result = getMeterProtocol().queryMessage(MessageEntry.fromContent(message).trackingId("").finish());
        System.out.println("Write LoadLimit duration : " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
    }

    @Override
    void doDebug() throws LinkException, IOException {
        ProfileGeneric profileGeneric = getMeterProtocol().getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.128.0.0.255"));
        byte[] bufferData = profileGeneric.getBufferData();
        PLCLog plcLog = new PLCLog(bufferData, getTimeZone());
    }
}
