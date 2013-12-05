package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.device.data.ProfileData;
import com.energyict.mdc.protocol.device.data.RegisterValue;
import com.energyict.mdc.protocol.device.events.MeterEvent;
import com.energyict.protocol.BubbleUpObject;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.PulseWeight;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.CurrentIndexReading;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.DailyConsumption;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.DataloggingTable;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.ExtendedIndexReading;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.GlobalIndexReading;
import com.energyict.protocolimpl.coronis.waveflow.waveflowV1.ProfileDataReaderV1;
import com.energyict.protocolimpl.coronis.waveflow.waveflowV2.ProfileDataReader;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 27-apr-2011
 * Time: 11:16:11
 */
public class BubbleUpFrameParser {

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final double MAX = 0x20;
    private static final int INITIAL_BATTERY_LIFE_COUNT = 0xC15C;
    private static PulseWeight[] pulseWeights = new PulseWeight[4];

    public static BubbleUpObject parse(byte[] data, WaveFlow waveflow) throws IOException {
        data = ProtocolTools.getSubArray(data, 6);          //Skip the radio address
        waveflow.enableInitialRFCommand();                  //To avoid new requests by the protocol

        int type = data[0] & 0xFF;
        type = type | 0x80;
        data = ProtocolTools.getSubArray(data, 1);

        switch (type) {
            case 0x81:
                return parseImmediateIndex(data, waveflow);
            case 0x83:
            case 0x87:
                return parseDataloggingTable(data, waveflow, type);
            case 0x85:
                return parseGlobalIndexes(data, waveflow);
            case 0x86:
                return parseExtendedIndex(data, waveflow);
            case 0xA7:
                return parseDailyConsumption(data, waveflow);
            default:
                throw new WaveFlowException("Unexpected bubble up frame. Expected type: 0x01, 0x03, 0x05, 0x06, 0x07 or 0x27");
        }
    }

    /**
     * Parses the RSSI level and battery level from the generic header
     *
     * @param data the generic header
     * @return 2 registers
     */
    private static List<RegisterValue> getGenericHeaderRegisters(byte[] data, WaveFlow waveFlow) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        double qos = ProtocolTools.getUnsignedIntFromBytes(data, 12, 1);
        qos = (qos / MAX) * 100;
        qos = Math.round(qos * 100.0) / 100.0;

        double shortLifeCounter = ProtocolTools.getUnsignedIntFromBytes(data, 13, 2);
        shortLifeCounter = 100 - (((INITIAL_BATTERY_LIFE_COUNT * 100) - (shortLifeCounter * 100)) / INITIAL_BATTERY_LIFE_COUNT);
        shortLifeCounter = Math.round(shortLifeCounter * 100.0) / 100.0;

        RegisterValue reg = new RegisterValue(ObisCode.fromString("0.0.96.6.0.255"), new Quantity(shortLifeCounter, Unit.get("")), new Date());
        registerValues.add(reg);

        PulseWeight pulseA = new PulseWeight(waveFlow, 1);
        PulseWeight pulseB = new PulseWeight(waveFlow, 2);
        PulseWeight pulseC = new PulseWeight(waveFlow, 3);
        PulseWeight pulseD = new PulseWeight(waveFlow, 4);
        pulseA.parse(new byte[] {data[15]});
        pulseB.parse(new byte[] {data[16]});
        pulseC.parse(new byte[] {data[17]});
        pulseD.parse(new byte[] {data[18]});

        pulseWeights = new PulseWeight[]{pulseA, pulseB, pulseC, pulseD};

        reg = new RegisterValue(ObisCode.fromString("0.0.96.0.63.255"), new Quantity(qos > 100 ? 100 : qos, Unit.get("")), new Date());
        registerValues.add(reg);
        return registerValues;
    }


    private static BubbleUpObject parseDailyConsumption(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        DailyConsumption dailyConsumption = new DailyConsumption(waveflow);
        dailyConsumption.setGenericHeaderLength(19);   //Instead of the usual 23!
        dailyConsumption.parse(data);
        registerValues.addAll(getGenericHeaderRegisters(data, waveflow));       //Parse the generic header, it contains the battery counter and RSSI level

        RegisterValue reg = new RegisterValue(ObisCode.fromString("1.1.82.8.0.255"), new Quantity(dailyConsumption.getIndexZone().getCurrentIndexOnA() * pulseWeights[0].getWeight(), pulseWeights[0].getUnit()), new Date());
        registerValues.add(reg);

        for (int port = 0; port < dailyConsumption.getNumberOfInputs(); port++) {
            int value = dailyConsumption.getIndexZone().getDailyIndexOnPort(port);
            reg = new RegisterValue(ObisCode.fromString("1." + (port + 1) + ".82.8.0.1"), new Quantity(value * pulseWeights[port].getWeight(), pulseWeights[port].getUnit()), new Date(), dailyConsumption.getIndexZone().getLastDailyLoggedIndex() );
            registerValues.add(reg);
        }

        Date lastLogged = dailyConsumption.getLastLoggedReading();
        ProfileDataReader profileDataReader = new ProfileDataReader(waveflow);

        profileDataReader.setNumberOfInputsUsed(dailyConsumption.getNumberOfInputs());
        profileDataReader.setInterval(dailyConsumption.getSamplingPeriod().getSamplingPeriodInSeconds());

        profileDatas.add(profileDataReader.parseProfileData(true, false, true, true, false, lastLogged, 0, 0, dailyConsumption, new Date(0), new Date(), null));
        result.setProfileDatas(profileDatas);
        result.setRegisterValues(registerValues);
        return result;
    }

    private static BubbleUpObject parseExtendedIndex(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        ExtendedIndexReading extendedIndexReading = new ExtendedIndexReading(waveflow);
        extendedIndexReading.parse(data);
        ProfileDataReader profileDataReader = new ProfileDataReader(waveflow);
        Date lastLogged = extendedIndexReading.getDateOfLastLoggedValue();
        List<Long[]> last4loggedIndexes = extendedIndexReading.getLast4LoggedIndexes();

        OperatingMode operatingMode = new OperatingMode(waveflow, extendedIndexReading.getOperationMode());
        profileDataReader.setNumberOfInputsUsed(operatingMode.getNumberOfInputsUsed());
        profileDataReader.setInterval(extendedIndexReading.getDataloggingMeasurementPeriod().getSamplingPeriodInSeconds());
        if (operatingMode.isWeeklyMeasurement()) {
            profileDataReader.setInterval(WEEKLY);
        }
        profileDatas.add(profileDataReader.parseProfileData(true, false, true, false, operatingMode.isMonthlyMeasurement(), lastLogged, 0, 0, null, new Date(0), new Date(), last4loggedIndexes));
        result.setProfileDatas(profileDatas);

        for (int input = 0; input < extendedIndexReading.getNumberOfEnabledInputs(); input++) {
            int value = extendedIndexReading.getIndexOfLastMonth(input);
            PulseWeight pulseWeight = waveflow.getPulseWeight(input, false);
            RegisterValue reg = new RegisterValue(ObisCode.fromString("1." + String.valueOf(input + 1) + ".82.8.0.0"), new Quantity(value * pulseWeight.getWeight(), pulseWeight.getUnit()), new Date(), parseDateOfLastMonthsEnd(extendedIndexReading.getDateOfLastLoggedValue()).getTime());
            registerValues.add(reg);
            int value2 = extendedIndexReading.getCurrentIndex(input);
            RegisterValue reg2 = new RegisterValue(ObisCode.fromString("1." + String.valueOf(input + 1) + ".82.8.0.255"), new Quantity(value2 * pulseWeight.getWeight(), pulseWeight.getUnit()), new Date());
            registerValues.add(reg2);
        }
        result.setRegisterValues(registerValues);
        return result;
    }

    private static Calendar parseDateOfLastMonthsEnd(Date dateOfLastLoggedValue) throws IOException {
        Calendar calLastOfMonth = new GregorianCalendar(TimeZone.getDefault());
        calLastOfMonth.setTime(dateOfLastLoggedValue);
        calLastOfMonth.set(Calendar.DATE, 1);
        calLastOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        calLastOfMonth.set(Calendar.MINUTE, 0);
        calLastOfMonth.set(Calendar.SECOND, 0);
        calLastOfMonth.set(Calendar.MILLISECOND, 0);
        return calLastOfMonth;
    }

    private static BubbleUpObject parseGlobalIndexes(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        GlobalIndexReading globalIndexReading = new GlobalIndexReading(waveflow);
        int operationMode = data[0] & 0xFF;
        int applicationStatus = data[1] & 0xFF;
        OperatingMode operatingMode = new OperatingMode(waveflow, operationMode);
        data = ProtocolTools.getSubArray(data, 2);  //Skip first 2 bytes
        globalIndexReading.parse(data, operatingMode.getNumberOfInputsUsed());

        int index = 0;
        for (long value : globalIndexReading.getReadings()) {
            RegisterValue reg = new RegisterValue(ObisCode.fromString("1." + String.valueOf(index + 1) + ".82.8.0.255"), new Quantity(value * waveflow.getPulseWeight(index, false).getWeight(), waveflow.getPulseWeight(index, false).getUnit()), new Date());
            registerValues.add(reg);
            index++;
        }
        result.setRegisterValues(registerValues);

        List<ProfileData> profileDatas = getMeterEventsFromApplicationStatus(waveflow, applicationStatus);
        result.setProfileDatas(profileDatas);

        return result;
    }

    public static BubbleUpObject parseDataloggingTable(byte[] data, WaveFlow waveflow, int type) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        Date lastLogged;
        int firstByte = data[0] & 0xFF;
        if (firstByte == 0xFF && data.length == 1) {
            return new BubbleUpObject();                    //Data logging table is empty
        }

        OperatingMode operatingMode = new OperatingMode(waveflow, firstByte);

        int channels = 0;
        int numberOfInputsUsed = operatingMode.getNumberOfInputsUsed();
        int[] channelIndexes = new int[0];
        if (numberOfInputsUsed == 1) {
            channels = 1;
            channelIndexes = new int[]{1};
        }
        if (numberOfInputsUsed == 2) {
            channels = 12;
            channelIndexes = new int[]{1, 2};
        }
        if (numberOfInputsUsed == 3) {
            channels = 12;
            channelIndexes = new int[]{1, 2};
            if (type == 0x87) {
                channels = 3;
                channelIndexes = new int[]{3};
            }
        }
        if (numberOfInputsUsed == 4) {
            channels = 12;
            channelIndexes = new int[]{1, 2};
            if (type == 0x87) {
                channels = 34;
                channelIndexes = new int[]{3, 4};
            }
        }
        DataloggingTable dataloggingTable = new DataloggingTable(waveflow);
        dataloggingTable.setChannels(channels);

        dataloggingTable.parse(data);
        List<Long[]> rawValues = new ArrayList<Long[]>();
        lastLogged = dataloggingTable.getLastLoggedIndexDate();

        for (int port = 0; port < 4; port++) {
            if (dataloggingTable.getProfileData(port) != null && dataloggingTable.getProfileData(port).length > 0) {
                rawValues.add(dataloggingTable.getProfileData(port));
            }
        }

        ProfileDataReaderV1 profileDataReaderV1 = new ProfileDataReaderV1(waveflow);
        profileDataReaderV1.setProfileInterval(dataloggingTable.getDataloggingMeasurementPeriod().getSamplingPeriodInSeconds());
        if (operatingMode.isWeeklyMeasurement()) {
            profileDataReaderV1.setProfileInterval(WEEKLY);
        }
        int inputsUsed = (channels == 1 || channels == 3) ? 1 : 2;
        int nrOfReadings = (inputsUsed == 1 ? ((channels == 3) ? 12 : 24) : 12);

        profileDatas.add(profileDataReaderV1.parseProfileData(true, false, inputsUsed, channelIndexes, nrOfReadings, operatingMode.isMonthlyMeasurement(), new Date(0), new Date(), true, rawValues, lastLogged));
        result.setProfileDatas(profileDatas);
        return result;
    }

    private static BubbleUpObject parseImmediateIndex(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        CurrentIndexReading indexReading = new CurrentIndexReading(waveflow);
        indexReading.parse(data);

        List<ProfileData> profileDatas = getMeterEventsFromApplicationStatus(waveflow, indexReading.getApplicationStatus());
        result.setProfileDatas(profileDatas);

        RegisterValue readingA = new RegisterValue(ObisCode.fromString("1.1.82.8.0.255"), new Quantity(indexReading.getReadings(0) * waveflow.getPulseWeight(0, false).getWeight(), waveflow.getPulseWeight(0, false).getUnit()), new Date());
        RegisterValue readingB = new RegisterValue(ObisCode.fromString("1.2.82.8.0.255"), new Quantity(indexReading.getReadings(1) * waveflow.getPulseWeight(1, false).getWeight(), waveflow.getPulseWeight(1, false).getUnit()), new Date());

        registerValues.add(readingA);
        registerValues.add(readingB);
        result.setRegisterValues(registerValues);
        return result;
    }

    /**
     * Parse the application status into a list of meter events and return them in a profile data object
     *
     * @param waveflow          reference to the protocol containing the properties
     * @param applicationStatus the status indicating the events
     * @return list of profile datas containing the parsed events
     * @throws java.io.IOException
     */
    private static List<ProfileData> getMeterEventsFromApplicationStatus(WaveFlow waveflow, int applicationStatus) throws IOException {
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        ProfileData profileData = new ProfileData();
        ApplicationStatusParser parser = new ApplicationStatusParser(waveflow, true);
        List<MeterEvent> meterEvents = parser.getMeterEvents(true, applicationStatus, false);
        profileData.setMeterEvents(meterEvents);
        profileDatas.add(profileData);
        return profileDatas;
    }
}