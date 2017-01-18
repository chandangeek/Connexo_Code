package com.energyict.protocols.mdc.inbound.general.frames;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.protocols.mdc.inbound.general.frames.parsing.EventInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 26/06/12
 * Time: 9:52
 * Author: khe
 */
public class EventFrame extends AbstractInboundFrame {

    private static final String EVENT_TAG = "event";

    private final Thesaurus thesaurus;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    @Override
    protected FrameType getType() {
        return FrameType.EVENT;
    }

    public EventFrame(String frame, IssueService issueService, Thesaurus thesaurus, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        super(frame, issueService, identificationService);
        this.thesaurus = thesaurus;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    @Override
    public void doParse() {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        LogBookIdentifier logBookIdentifier;
        Device device = this.getDevice();

        if (!device.getLogBooks().isEmpty()) {
            logBookIdentifier = getIdentificationService().createLogbookIdentifierByObisCodeAndDeviceIdentifier(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE, getDeviceIdentifier());
        } else {
            getCollectedDatas().add(this.collectedDataFactory.createNoLogBookCollectedData(getDeviceIdentifier()));
            return;
        }

        CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(logBookIdentifier);
        for (String parameter : this.getParameters()) {
            if (parameter.contains(EVENT_TAG)) {
                String[] nameAndEvent = parameter.split("=");
                if (nameAndEvent.length == 2) {
                    EventInfo eventInfo = new EventInfo(nameAndEvent[1]);
                    Optional<MeterProtocolEvent> meterProtocolEvent = eventInfo.parse(this.thesaurus, this.meteringService);
                    if (meterProtocolEvent.isPresent()) {
                        meterEvents.add(meterProtocolEvent.get());
                    }
                    else {
                        deviceLogBook.setFailureInformation(
                                ResultType.NotSupported,
                                this.getIssueService().newWarning(
                                        deviceLogBook,
                                        MessageSeeds.END_DEVICE_EVENT_TYPE_NOT_SUPPORTED,
                                        EndDeviceEventTypeMapping.OTHER.getEndDeviceEventTypeMRID()));
                    }
                }
            }
        }
        deviceLogBook.setMeterEvents(meterEvents);
        getCollectedDatas().add(deviceLogBook);
    }

}