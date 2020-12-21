package com.energyict.protocolimplv2.dlms.as3000.readers;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.as3000.readers.logbook.AS3000StandardEventMapper;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.CollectedLogBookBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.GenericLogBookReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper.EventMapper;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.DLMSReaderRegistry;

public class AS3000ReadableLogbook {

    private final CollectedLogBookBuilder collectedLogBookBuilder;

    public AS3000ReadableLogbook(CollectedLogBookBuilder collectedLogBookBuilder) {
        this.collectedLogBookBuilder = collectedLogBookBuilder;
    }

    public CollectedLogBookReader getCollectedLogBookReader(AbstractDlmsProtocol dlmsProtocol) {
        DLMSReaderRegistry<CollectedLogBook, LogBookReader, ObisCode> logBookReaders = new DLMSReaderRegistry<>();
        logBookReaders.add(genericEvent());
        return new CollectedLogBookReader(logBookReaders, dlmsProtocol, collectedLogBookBuilder);
    }

    private GenericLogBookReader genericEvent() {
        EventMapper eventMapper = new AS3000StandardEventMapper();
        return new GenericLogBookReader(eventMapper, new ObisCodeMatcher(ObisCode.fromString("1.1.99.98.0.255")), collectedLogBookBuilder);
    }

}
