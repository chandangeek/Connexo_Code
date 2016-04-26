package com.elster.jupiter.metering.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Operator;

import java.util.List;

public final class EndDeviceCapabilities {
    private final DataModel dataModel;
    private final EndDevice endDevice;

    public EndDeviceCapabilities(DataModel dataModel, EndDevice endDevice) {
        this.dataModel = dataModel;
        this.endDevice = endDevice;
    }

    List<ReadingType> getConfiguredReadingTypes() {
        return dataModel.query(ReadingType.class).select(Operator.EQUALIGNORECASE.compare("MRID", endDevice.getMRID()));

    }

    List<EndDeviceControlType> getSupportedEndDeviceControlTypes() {
        return dataModel.query(EndDeviceControlType.class).select(Operator.EQUALIGNORECASE.compare("MRID", endDevice.getMRID()));
        //return dataModel.mapper(EndDeviceControlType.class).find();
    }
}
