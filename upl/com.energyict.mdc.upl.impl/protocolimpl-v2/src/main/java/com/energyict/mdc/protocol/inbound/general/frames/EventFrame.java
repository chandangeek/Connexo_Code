package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.protocol.inbound.general.frames.parsing.EventInfo;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.mdw.core.LogBookTypeFactory;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

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
    private final CollectedDataFactory collectedDataFactory;

    @Override
    protected FrameType getType() {
        return FrameType.EVENT;
    }

    public EventFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder, CollectedDataFactory collectedDataFactory) {
        super(frame, callHomeIdPlaceHolder);
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public void doParse() {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        LogBookIdentifier logBookIdentifier = new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifierByDialHomeIdPlaceHolder(), LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE);

        for (String parameter : this.getParameters()) {
            if (parameter.contains(EVENT_TAG)) {
                String[] nameAndEvent = parameter.split("=");
                if (nameAndEvent.length == 2) {
                    EventInfo eventInfo = new EventInfo(nameAndEvent[1]);
                    MeterProtocolEvent meterProtocolEvent = eventInfo.parse();
                    meterEvents.add(meterProtocolEvent);
                }
            }
        }
        if (!meterEvents.isEmpty()) {
            CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(logBookIdentifier);
            deviceLogBook.setCollectedMeterEvents(meterEvents);
            getCollectedDatas().add(deviceLogBook);
        }
    }
}