package com.energyict.protocolimpl.coronis.waveflow.hydreka;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.*;
import com.energyict.protocolimpl.coronis.waveflow.hydreka.radiocommand.DailyHydrekaDataReading;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 17/12/12
 * Time: 17:32
 * Author: khe
 */
public class BubbleUpFrameParser {

    public static BubbleUpObject parseFrame(byte[] rawData, Hydreka waveFlow) throws IOException {

        waveFlow.enableInitialRFCommand();                              //No extra requests allowed in further protocol flow
        byte[] data = ProtocolTools.getSubArray(rawData, 6);            //Skip the radio address
        int type = data[0] & 0xFF;
        type = type | 0x80;
        data = ProtocolTools.getSubArray(data, 1);

        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        List<RegisterValue> registers = new ArrayList<RegisterValue>();
        if (type == 0xA7) {
            DailyHydrekaDataReading dailyHydrekaDataReading = new DailyHydrekaDataReading(waveFlow);
            dailyHydrekaDataReading.parseFromBubbleUp(data);
            waveFlow.getParameterFactory().setLeakageDetectionDate(dailyHydrekaDataReading.getLeakageDetectionDate());  //Cache for further usage
            waveFlow.getRadioCommandFactory().setDailyHydrekaDataReading(dailyHydrekaDataReading);
            registers.addAll(getGenericHeaderRegisters(waveFlow));

            int status = waveFlow.getParameterFactory().readApplicationStatus();
            ApplicationStatusParser parser = new ApplicationStatusParser(waveFlow, true);
            meterEvents.addAll(parser.getMeterEvents(status));
        } else {
            throw new WaveFlowException("Unexpected bubble up frame. Expected type 0x27");
        }

        ArrayList<ProfileData> profileDatas = new ArrayList<ProfileData>();
        ProfileData profileData = new ProfileData();
        profileData.setMeterEvents(meterEvents);
        profileDatas.add(profileData);
        BubbleUpObject bubbleUpObject = new BubbleUpObject();
        bubbleUpObject.setProfileDatas(profileDatas);
        bubbleUpObject.setRegisterValues(registers);
        return bubbleUpObject;
    }

    /**
     * Parses the RSSI level, battery level,... from the generic header
     */
    private static List<RegisterValue> getGenericHeaderRegisters(Hydreka waveFlow) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();

        RegisterValue reg = new RegisterValue(CommonObisCodeMapper.OBISCODE_REMAINING_BATTERY, new Quantity(waveFlow.getParameterFactory().readBatteryLifeDurationCounter(), Unit.get("")), new Date());
        registerValues.add(reg);

        double rssiLevel = waveFlow.getRadioCommandFactory().readRSSILevel();
        reg = new RegisterValue(CommonObisCodeMapper.OBISCODE_RSSI_LEVEL, new Quantity(rssiLevel > 100 ? 100 : rssiLevel, Unit.get("")), new Date());
        registerValues.add(reg);

        reg = new RegisterValue(CommonObisCodeMapper.OBISCODE_APPLICATION_STATUS, new Quantity(waveFlow.getParameterFactory().readApplicationStatus(), Unit.get("")), new Date());
        registerValues.add(reg);

        reg = new RegisterValue(CommonObisCodeMapper.OBISCODE_OPERATION_MODE, new Quantity(waveFlow.getParameterFactory().readOperatingMode().getOperationMode(), Unit.get("")), new Date());
        registerValues.add(reg);

        byte[] rawData = waveFlow.getRadioCommandFactory().readDailyHydrekaDataReading().getRawData();
        reg = new RegisterValue(ObisCodeMapperHydreka.OBISCODE_DAILY_HYDREKA_DATA, ProtocolTools.getHexStringFromBytes(rawData, ""));
        registerValues.add(reg);

        return registerValues;
    }
}