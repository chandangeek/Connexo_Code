package com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterProtocolEvent;

import java.util.List;

public interface EventMapper {
    List<MeterProtocolEvent> map(DataContainer dataContainer);
}
