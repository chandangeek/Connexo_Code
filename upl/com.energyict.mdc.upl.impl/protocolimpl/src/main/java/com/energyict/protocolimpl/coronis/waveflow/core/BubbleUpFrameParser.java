package com.energyict.protocolimpl.coronis.waveflow.core;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.core.BubbleUpObject;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.*;
import com.energyict.protocolimpl.coronis.waveflow.waveflowV1.ProfileDataReaderV1;
import com.energyict.protocolimpl.coronis.waveflow.waveflowV2.ProfileDataReader;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 27-apr-2011
 * Time: 11:16:11
 */
public class BubbleUpFrameParser {

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = WEEKLY * 4;

    public static BubbleUpObject parse(byte[] data, WaveFlow waveflow) throws IOException {
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

    private static BubbleUpObject parseDailyConsumption(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();

        DailyConsumption dailyConsumption = new DailyConsumption(waveflow);
        dailyConsumption.parse(data);
        Date lastLogged = dailyConsumption.getLastLoggedReading();
        ProfileDataReader profileDataReader = new ProfileDataReader(waveflow);

        profileDataReader.setNumberOfInputsUsed(dailyConsumption.getNumberOfInputs());
        profileDataReader.setInterval(DAILY);

        profileDatas.add(profileDataReader.parseProfileData(false, false, true, false, lastLogged, 0, 0, dailyConsumption, new Date(0), new Date(), null));
        result.setProfileDatas(profileDatas);
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

        boolean monthly = false;
        OperatingMode operatingMode = new OperatingMode(waveflow, extendedIndexReading.getOperationMode());
        if (operatingMode.isMonthlyMeasurement()) {
            monthly = true;
        }
        profileDataReader.setNumberOfInputsUsed(operatingMode.getNumberOfInputsUsed());
        profileDataReader.setInterval(extendedIndexReading.getDataloggingMeasurementPeriod().getSamplingPeriodInSeconds());
        if (operatingMode.isMonthlyMeasurement()) {
            profileDataReader.setInterval(MONTHLY);
        }
        if (operatingMode.isWeeklyMeasurement()) {
            profileDataReader.setInterval(WEEKLY);
        }
        profileDatas.add(profileDataReader.parseProfileData(false, false, false, monthly, lastLogged, 0, 0, null, new Date(0), new Date(), last4loggedIndexes));
        result.setProfileDatas(profileDatas);

        for (int input = 0; input < extendedIndexReading.getNumberOfEnabledInputs(); input++) {
            int value = extendedIndexReading.getIndexOfLastMonth(input);
            RegisterValue reg = new RegisterValue(ObisCode.fromString("1." + String.valueOf(input + 1) + ".82.8.0.0"), new Quantity(value, Unit.get("")), new Date());
            registerValues.add(reg);
            int value2 = extendedIndexReading.getCurrentIndex(input);
            RegisterValue reg2 = new RegisterValue(ObisCode.fromString("1." + String.valueOf(input + 1) + ".82.8.0.255"), new Quantity(value2, Unit.get("")), new Date());
            registerValues.add(reg2);
        }
        result.setRegisterValues(registerValues);
        return result;
    }

    private static BubbleUpObject parseGlobalIndexes(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        GlobalIndexReading globalIndexReading = new GlobalIndexReading(waveflow);
        globalIndexReading.parse(data);
        int index = 0;

        for (long value : globalIndexReading.getReadings()) {
            RegisterValue reg = new RegisterValue(ObisCode.fromString("1." + String.valueOf(index + 1) + ".82.8.0.255"), new Quantity(value, Unit.get("")), new Date());
            registerValues.add(reg);
            index++;
        }
        result.setRegisterValues(registerValues);

        return result;
    }

    private static BubbleUpObject parseDataloggingTable(byte[] data, WaveFlow waveflow, int type) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        Date lastLogged;
        OperatingMode operatingMode = new OperatingMode(waveflow, data[0] & 0xFF);

        int channels = 0;
        int numberOfInputsUsed = operatingMode.getNumberOfInputsUsed();
        if (numberOfInputsUsed == 1) {
            channels = 1;
        }
        if (numberOfInputsUsed == 2) {
            channels = 12;
        }
        if (numberOfInputsUsed == 3) {
            channels = 12;
            if (type == 0x87) {
                channels = 3;
            }
        }
        if (numberOfInputsUsed == 4) {
            channels = 12;
            if (type == 0x87) {
                channels = 34;
            }
        }
        DataloggingTable dataloggingTable = new DataloggingTable(waveflow);
        dataloggingTable.setChannels(channels);

        dataloggingTable.parse(data);
        List<Long[]> rawValues = new ArrayList<Long[]>();
        lastLogged = dataloggingTable.getLastLoggedIndexDate();
        rawValues.add(dataloggingTable.getProfileDataA());
        rawValues.add(dataloggingTable.getProfileDataB());

        ProfileDataReaderV1 profileDataReaderV1 = new ProfileDataReaderV1(waveflow);
        profileDataReaderV1.setProfileInterval(dataloggingTable.getDataloggingMeasurementPeriod().getSamplingPeriodInSeconds());
        if (operatingMode.isMonthlyMeasurement()) {
            profileDataReaderV1.setProfileInterval(MONTHLY);
        }
        if (operatingMode.isWeeklyMeasurement()) {
            profileDataReaderV1.setProfileInterval(WEEKLY);
        }

        profileDatas.add(profileDataReaderV1.parseProfileData(false, false, new Date(0), new Date(), false, rawValues, lastLogged));
        result.setProfileDatas(profileDatas);
        return result;
    }

    private static BubbleUpObject parseImmediateIndex(byte[] data, WaveFlow waveflow) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        CurrentIndexReading indexReading = new CurrentIndexReading(waveflow);
        indexReading.parse(data);
        RegisterValue readingA = new RegisterValue(ObisCode.fromString("1.1.82.8.0.255"), new Quantity(indexReading.getReadings(0), Unit.get("")), new Date());
        RegisterValue readingB = new RegisterValue(ObisCode.fromString("1.2.82.8.0.255"), new Quantity(indexReading.getReadings(1), Unit.get("")), new Date());

        registerValues.add(readingA);
        registerValues.add(readingB);
        result.setRegisterValues(registerValues);
        return result;
    }
}