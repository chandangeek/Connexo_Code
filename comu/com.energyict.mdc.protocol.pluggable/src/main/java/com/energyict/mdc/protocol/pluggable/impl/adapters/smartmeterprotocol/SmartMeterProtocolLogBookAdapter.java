package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter between a {@link SmartMeterProtocolAdapter} and {@link DeviceLogBookSupport}
 *
 * @author gna
 * @since 10/04/12 - 15:29
 */
public class SmartMeterProtocolLogBookAdapter implements DeviceLogBookSupport {

    /**
     * The used {@link SmartMeterProtocol}
     */
    private final SmartMeterProtocol smartMeterProtocol;

    public SmartMeterProtocolLogBookAdapter(final SmartMeterProtocol smartMeterProtocol) {
        this.smartMeterProtocol = smartMeterProtocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(final List<LogBookReader> logBookReaders) {
        CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>();
        if (logBookReaders != null && this.smartMeterProtocol != null) {
            for (LogBookReader reader : logBookReaders) {
                CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(reader.getLogBookIdentifier());
                try {
                    if (reader.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                        final List<MeterEvent> meterEvents = smartMeterProtocol.getMeterEvents(reader.getLastLogBook());
                        deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
                    } else {
                        deviceLogBook.setFailureInformation(ResultType.NotSupported, reader.getLogBookObisCode(), "logBookXnotsupported", reader.getLogBookObisCode());
                    }
                } catch (IOException e) {
                    deviceLogBook.setFailureInformation(ResultType.InCompatible, reader.getLogBookObisCode(), "logBookXissue", reader.getLogBookObisCode(), e);
                }
                collectedLogBooks.add(deviceLogBook);
            }
        }
        return collectedLogBooks;
    }

    private CollectedDataFactory getCollectedDataFactory() {
        List<CollectedDataFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(CollectedDataFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(CollectedDataFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}