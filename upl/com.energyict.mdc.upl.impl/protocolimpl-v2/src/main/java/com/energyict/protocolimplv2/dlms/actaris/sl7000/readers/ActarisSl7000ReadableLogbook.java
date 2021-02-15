package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.logbook.ActarisSl7000EventMapper;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.CollectedLogBookBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.GenericLogBookReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.mapper.EventMapper;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.ReaderRegistry;

import java.util.ArrayList;
import java.util.List;

public class ActarisSl7000ReadableLogbook {

    private final CollectedLogBookBuilder collectedLogBookBuilder;

    public ActarisSl7000ReadableLogbook(CollectedLogBookBuilder collectedLogBookBuilder) {
        this.collectedLogBookBuilder = collectedLogBookBuilder;
    }

    public CollectedLogBookReader<ActarisSl7000> getCollectedLogBookReader(ActarisSl7000 dlmsProtocol) {
        List<ObisReader<CollectedLogBook, LogBookReader, ObisCode, ActarisSl7000>> readers = new ArrayList<>();
        readers.add(genericEvent());
        ReaderRegistry<CollectedLogBook, LogBookReader, ObisCode, ActarisSl7000> logBookReaders = new ReaderRegistry<>(readers);
        return new CollectedLogBookReader<>(logBookReaders, dlmsProtocol, collectedLogBookBuilder);
    }

    private GenericLogBookReader<ActarisSl7000> genericEvent() {
        EventMapper eventMapper = new ActarisSl7000EventMapper();
        return new GenericLogBookReader<>(eventMapper, new ObisCodeMatcher(ObisCode.fromString("0.0.99.98.0.255")), collectedLogBookBuilder);
    }

}
