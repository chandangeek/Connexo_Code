package com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.util.List;

public interface EventMapper {

    List<MeterProtocolEvent> map(AbstractDlmsProtocol protocol, LogBookReader lbr, DataContainer dataContainer);

}
