package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.buffer;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.IntervalData;

import java.util.List;

public interface BufferParser {

    List<IntervalData> parse(DataContainer buffer) throws ProtocolException;
}
