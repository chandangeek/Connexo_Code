package com.energyict.protocolimplv2.dlms.as3000.readers;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.as3000.AS3000;
import com.energyict.protocolimplv2.dlms.as3000.readers.logbook.AS3000StandardEventMapper;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.CollectedLogBookBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.GenericLogBookReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper.EventMapper;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.ReaderRegistry;

import java.util.ArrayList;
import java.util.List;

public class AS3000ReadableLogbook {

    private final CollectedLogBookBuilder collectedLogBookBuilder;

    public AS3000ReadableLogbook(CollectedLogBookBuilder collectedLogBookBuilder) {
        this.collectedLogBookBuilder = collectedLogBookBuilder;
    }

    public CollectedLogBookReader<AS3000> getCollectedLogBookReader(AS3000 dlmsProtocol) {
        List<ObisReader<CollectedLogBook, LogBookReader, ObisCode, AS3000>> logBookReaders = new ArrayList<>();
        logBookReaders.add(genericEvent());
        ReaderRegistry<CollectedLogBook, LogBookReader, ObisCode, AS3000> lbrs = new ReaderRegistry<>(logBookReaders);
        return new CollectedLogBookReader<>(lbrs, dlmsProtocol, collectedLogBookBuilder);
    }

    private GenericLogBookReader<AS3000> genericEvent() {
        EventMapper eventMapper = new AS3000StandardEventMapper();
        return new GenericLogBookReader<>(eventMapper, new ObisCodeMatcher(ObisCode.fromString("1.1.99.98.0.255")), collectedLogBookBuilder);
    }

}
