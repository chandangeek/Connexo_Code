package com.energyict.protocols.mdc.inbound.general.frames;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.inbound.general.frames.parsing.EventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 9:52
 * Author: khe
 */
public class EventFrame extends AbstractInboundFrame {

    private static final String EVENT_TAG = "event";

    private final Thesaurus thesaurus;

    @Override
    protected FrameType getType() {
        return FrameType.EVENT;
    }

    public EventFrame(String frame, IssueService issueService, Thesaurus thesaurus, IdentificationService identificationService) {
        super(frame, issueService, identificationService);
        this.thesaurus = thesaurus;
    }

    @Override
    public void doParse() {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        LogBookIdentifier logBookIdentifier;
        BaseDevice device = this.getDevice();

        if (!device.getLogBooks().isEmpty()) {
            logBookIdentifier = getIdentificationService().createLogbookIdentifierByObisCodeAndDeviceIdentifier(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE, getDeviceIdentifier());
        } else {
            getCollectedDatas().add(this.getCollectedDataFactory().createNoLogBookCollectedData(getDeviceIdentifier()));
            return;
        }

        for (String parameter : this.getParameters()) {
            if (parameter.contains(EVENT_TAG)) {
                String[] nameAndEvent = parameter.split("=");
                if (nameAndEvent.length == 2) {
                    EventInfo eventInfo = new EventInfo(nameAndEvent[1]);
                    MeterProtocolEvent meterProtocolEvent = eventInfo.parse(this.thesaurus);
                    meterEvents.add(meterProtocolEvent);
                }
            }
        }
        if (!meterEvents.isEmpty()) {
            CollectedLogBook deviceLogBook = this.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
            deviceLogBook.setMeterEvents(meterEvents);
            getCollectedDatas().add(deviceLogBook);
        }
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}