package com.energyict.protocolimplv2.dlms.common.readers;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.CollectedLogBookBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CollectedLogBookReader implements DeviceLogBookSupport {

    private final DLMSReaderRegistry<CollectedLogBook, LogBookReader, ObisCode> specificReadableRegisters;

    private final AbstractDlmsProtocol dlmsProtocol;
    private final CollectedLogBookBuilder collectedLogBookBuilder;

    public CollectedLogBookReader(DLMSReaderRegistry<CollectedLogBook, LogBookReader, ObisCode> specificReadableRegisters, AbstractDlmsProtocol dlmsProtocol, CollectedLogBookBuilder collectedLogBookBuilder) {
        this.specificReadableRegisters = specificReadableRegisters;
        this.collectedLogBookBuilder = collectedLogBookBuilder;
        this.dlmsProtocol = dlmsProtocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<com.energyict.protocol.LogBookReader> logBookReaders) {
        List<CollectedLogBook> logBooks = new ArrayList<>(logBookReaders.size());
        for (com.energyict.protocol.LogBookReader lbr : logBookReaders) {
            Optional<ObisReader<CollectedLogBook, LogBookReader, ObisCode>> optionalReader = specificReadableRegisters.from(lbr.getLogBookObisCode());
            if (optionalReader.isPresent()) {
                ObisReader<CollectedLogBook, LogBookReader, ObisCode> reader = optionalReader.get();
                logBooks.add(reader.read(dlmsProtocol, lbr));
            } else {
                logBooks.add(collectedLogBookBuilder.createLogBook(lbr, ResultType.NotSupported,"No reader found for:" + lbr.getLogBookObisCode()));
            }
        }
        return logBooks;
    }

}
