package com.energyict.protocols.mdc.inbound.general.frames;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 25/06/12
 * Time: 11:08
 * Author: khe
 */
public class EventPOFrame extends AbstractInboundFrame {

    private static final int UNKNOWN = 0;
    private static final String EVENT_TAG = "event";

    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    @Override
    protected FrameType getType() {
        return FrameType.EVENTP0;
    }

    public EventPOFrame(String frame, IssueService issueService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        super(frame, issueService, identificationService);
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
                    Date timeStamp = parseTimeStampString(nameAndEvent[1]);
                    Optional<EndDeviceEventType> powerDownEventType = EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN, this.meteringService);
                    if (powerDownEventType.isPresent()) {
                        meterEvents.add(
                                new MeterProtocolEvent(timeStamp,
                                        MeterEvent.POWERDOWN,
                                        UNKNOWN,
                                        powerDownEventType.get(),
                                        "Last gas power outage",
                                        UNKNOWN,
                                        UNKNOWN));
                    }
                    else {
                        deviceLogBook.setFailureInformation(
                                ResultType.NotSupported,
                                this.getIssueService().newWarning(
                                        deviceLogBook,
                                        MessageSeeds.END_DEVICE_EVENT_TYPE_NOT_SUPPORTED,
                                        EndDeviceEventTypeMapping.POWERDOWN.getEndDeviceEventTypeMRID()));
                    }
                }
            }
        }
        deviceLogBook.setMeterEvents(meterEvents);
        getCollectedDatas().add(deviceLogBook);
    }

    private Date parseTimeStampString(String timeStampString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return formatter.parse(timeStampString);
        } catch (ParseException e) {
            return new Date();
        }
    }

}