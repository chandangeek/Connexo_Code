package com.energyict.protocolimplv2.dlms.common.obis.readers.logbook;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;

public interface LogBookReader<T, L extends AbstractDlmsProtocol> extends ObisReader<CollectedLogBook, com.energyict.protocol.LogBookReader, T, L> {
}
