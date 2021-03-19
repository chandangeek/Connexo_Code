package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.data;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;

import java.io.IOException;
import java.util.List;

public interface BufferParser {

    List<IntervalData> parse(DataContainer buffer, CollectedLoadProfileConfiguration collectedLoadProfileConfiguration, LoadProfileReader lpr) throws IOException;
}
