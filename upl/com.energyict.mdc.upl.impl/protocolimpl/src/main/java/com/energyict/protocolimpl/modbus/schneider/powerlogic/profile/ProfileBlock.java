package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ProfileBlock {

    private ProfileRecord profileRecord;

    public ProfileBlock(byte[] values, int[] loadProfileRecordItems) throws IOException {
        this.profileRecord = ProfileRecordParser.parse(values, getLoadProfileRecordItemsDataTypes(loadProfileRecordItems));
    }

    private List<ChannelConfigMapping.DataType> getLoadProfileRecordItemsDataTypes(int[] loadProfileRecordItems) throws IOException {
        List<ChannelConfigMapping.DataType> dataTypes = new ArrayList<>();
        for (int loadProfileRecordItem : loadProfileRecordItems) {
            if (loadProfileRecordItem != 0) {
                dataTypes.add(ChannelConfigMapping.findChannelConfigurationFor(loadProfileRecordItem).getDataType());
            }
        }
        return dataTypes;
    }

    public ProfileRecord getProfileRecord() {
        return profileRecord;
    }

    public IntervalData getIntervalData() {
        List<IntervalValue> intervalValues = new ArrayList<>();
        for (Object value : getProfileRecord().getValues()) {
            intervalValues.add(new IntervalValue((Number) value, 0, 0));
        }
        return new IntervalData(getProfileRecord().getDate(), IntervalStateBits.OK, 0, 0, intervalValues);
    }
}