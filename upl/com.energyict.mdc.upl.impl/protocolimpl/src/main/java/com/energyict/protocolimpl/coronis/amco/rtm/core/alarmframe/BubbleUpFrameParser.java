package com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.amco.rtm.ProfileDataReader;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.GenericHeader;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.*;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 27-apr-2011
 * Time: 11:16:11
 */
public class BubbleUpFrameParser {

    private static GenericHeader genericHeader;

    public static BubbleUpObject parse(byte[] data, RTM rtm) throws IOException {
        byte[] radioAddress = ProtocolTools.getSubArray(data, 0, 6);
        data = ProtocolTools.getSubArray(data, 6);

        int type = data[0] & 0xFF;
        type = type | 0x80;         //Not sure if type is the command or it's ack, so convert it to an ack
        data = ProtocolTools.getSubArray(data, 1);

        switch (type) {
            case 0x81:
                return parseCurrentIndexes(data, rtm, radioAddress);
            case 0x83:
                return parseDailyConsumption(data, rtm, radioAddress);
            case 0x86:
                return parseTouBucketsReading(data, rtm, radioAddress);
            case 0x87:
                return parseDataloggingTable(data, rtm, radioAddress);
            default:
                throw new WaveFlowException("Unexpected bubble up frame. Expected type: 0x01, 0x03, 0x06 or 0x07");
        }
    }

    private static BubbleUpObject parseDataloggingTable(byte[] data, RTM rtm, byte[] radioAddress) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        ExtendedDataloggingTable table = new ExtendedDataloggingTable(rtm);
        table.parseBubbleUpData(data);
        List<List<Integer>> profileData = table.getProfileDataForAllPorts();
        ProfileDataReader profileDataReader = new ProfileDataReader(rtm);
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        registerValues.addAll(getGenericHeaderRegisters(data, radioAddress));

        int numberOfPorts = 0;
        for (List<Integer> values : profileData) {
            if (values.size() > 0) {
                numberOfPorts++;
            }
        }

        OperatingMode operatingMode = new OperatingMode(rtm, table.getOperationMode());
        boolean monthly = operatingMode.isMonthlyLogging();
        profileDataReader.setNumberOfInputs(numberOfPorts);
        profileDataReader.setProfileInterval(table.getProfileInterval());

        profileDatas.add(profileDataReader.parseProfileData(genericHeader, false, profileData, new ProfileData(), monthly, false, new Date(), new Date(0), table.getLastLoggedTimeStamp(), 0));
        result.setProfileDatas(profileDatas);
        result.setRegisterValues(registerValues);
        return result;
    }

    private static BubbleUpObject parseTouBucketsReading(byte[] data, RTM rtm, byte[] radioAddress) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ReadTOUBuckets readTOUBuckets = new ReadTOUBuckets(rtm);
        readTOUBuckets.parse(data);
        registerValues.addAll(getGenericHeaderRegisters(data, radioAddress));

        for (int port = 0; port < readTOUBuckets.getNumberOfPorts(); port++) {
            int value = readTOUBuckets.getListOfAllTotalizers().get(port).getCurrentReading();
            int multiplier = genericHeader.getRtmUnit(port).getMultiplier();
            RegisterValue reg = new RegisterValue(ObisCode.fromString("1." + (port + 1) + ".82.8.0.255"), new Quantity(value * multiplier, genericHeader.getUnit(port)), new Date());
            registerValues.add(reg);

            for (int bucket = 1; bucket < 7; bucket++) {
                ObisCode obisCode = ObisCode.fromString("0." + (port + 1) + ".96.0." + (52 + bucket) + ".255");
                value = readTOUBuckets.getListOfAllTotalizers().get(port).getTOUBucketsTotalizers()[bucket - 1];
                reg = new RegisterValue(obisCode, new Quantity(value * multiplier, genericHeader.getUnit(port)), new Date());
                registerValues.add(reg);
            }
        }
        result.setRegisterValues(registerValues);
        return result;
    }

    private static BubbleUpObject parseCurrentIndexes(byte[] data, RTM rtm, byte[] radioAddress) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        BubbleUpObject result = new BubbleUpObject();

        registerValues.addAll(getGenericHeaderRegisters(data, radioAddress));

        CurrentRegisterReading currentRegisterReading = new CurrentRegisterReading(rtm);
        currentRegisterReading.parse(data);

        RegisterValue reg;
        for (int port = 0; port < currentRegisterReading.getNumberOfPorts(); port++) {
            reg = new RegisterValue(ObisCode.fromString("1." + (port + 1) + ".82.8.0.255"), new Quantity(currentRegisterReading.getCurrentReading(port + 1) * genericHeader.getRtmUnit(port).getMultiplier(), genericHeader.getUnit(port)), new Date());
            registerValues.add(reg);
        }
        result.setRegisterValues(registerValues);
        return result;
    }

    /**
     * Parses the RSSI level and battery level from the generic header
     *
     * @param data         the generic header
     * @param radioAddress indicates the RTM type, important to know the initial battery level
     * @return 2 registers
     */
    private static List<RegisterValue> getGenericHeaderRegisters(byte[] data, byte[] radioAddress) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        genericHeader = new GenericHeader(radioAddress);
        genericHeader.parse(data);

        RegisterValue reg = new RegisterValue(ObisCode.fromString("0.0.96.6.0.255"), new Quantity(genericHeader.getShortLifeCounter(), Unit.get("")), new Date());
        registerValues.add(reg);

        reg = new RegisterValue(ObisCode.fromString("0.0.96.0.63.255"), new Quantity(genericHeader.getQos(), Unit.get("")), new Date());
        registerValues.add(reg);
        return registerValues;
    }

    private static BubbleUpObject parseDailyConsumption(byte[] data, RTM rtm, byte[] radioAddress) throws IOException {
        BubbleUpObject result = new BubbleUpObject();
        List<ProfileData> profileDatas = new ArrayList<ProfileData>();
        ProfileData profileData = new ProfileData();
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        DailyConsumption dailyConsumption = new DailyConsumption(rtm);
        dailyConsumption.parse(data);
        List<List<Integer>> rawValues = new ArrayList<List<Integer>>();

        registerValues.addAll(getGenericHeaderRegisters(data, radioAddress));

        for (int port = 0; port < dailyConsumption.getNumberOfPorts(); port++) {
            int value = dailyConsumption.getCurrentIndexes()[port];
            RegisterValue reg = new RegisterValue(ObisCode.fromString("1." + (port + 1) + ".82.8.0.255"), new Quantity(value * genericHeader.getRtmUnit(port).getMultiplier(), genericHeader.getUnit(port)), new Date());
            registerValues.add(reg);
        }
        result.setRegisterValues(registerValues);

        for (int port = 0; port < dailyConsumption.getNumberOfPorts(); port++) {
            rawValues.add(dailyConsumption.getDailyReadings(port));
        }

        ProfileDataReader profileDataReader = new ProfileDataReader(rtm);
        profileDataReader.setNumberOfInputs(dailyConsumption.getNumberOfPorts());
        profileDataReader.setProfileInterval(dailyConsumption.getProfileInterval());
        profileData = profileDataReader.parseProfileData(genericHeader, false, rawValues, profileData, false, true, new Date(), new Date(0), dailyConsumption.getLastLoggedValue(), 0);
        profileDatas.add(profileData);

        result.setProfileDatas(profileDatas);
        return result;
    }
}