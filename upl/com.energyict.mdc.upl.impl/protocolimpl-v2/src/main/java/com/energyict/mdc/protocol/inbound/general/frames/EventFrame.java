package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.inbound.general.frames.parsing.EventInfo;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LogBook;
import com.energyict.mdw.core.LogBookFactoryProvider;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierById;
import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;

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

    @Override
    protected FrameType getType() {
        return FrameType.EVENT;
    }

    public EventFrame(String frame, SerialNumberPlaceHolder serialNumberPlaceHolder) {
        super(frame, serialNumberPlaceHolder);
    }

    @Override
    public void doParse() {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        LogBookIdentifier logBookIdentifier;
        Device device = this.getDevice();
        LogBook genericLogBook = LogBookFactoryProvider.instance.get().getLogBookFactory().findGenericLogBook(device);


        if (!device.getLogBooks().isEmpty()) {
            logBookIdentifier = new LogBookIdentifierById(genericLogBook.getId());
        } else {
            getCollectedDatas().add(MdcManager.getCollectedDataFactory().createNoLogBookCollectedData(new DeviceIdentifierBySerialNumber(getInboundParameters().getSerialNumber())));
            return;
        }

        for (String parameter : this.getParameters()) {
            if (parameter.contains(EVENT_TAG)) {
                String[] nameAndEvent = parameter.split("=");
                if (nameAndEvent.length == 2) {
                    EventInfo eventInfo = new EventInfo(nameAndEvent[1], genericLogBook.getId());
                    MeterProtocolEvent meterProtocolEvent = eventInfo.parse();
                    meterEvents.add(meterProtocolEvent);
                }
            }
        }
        if (!meterEvents.isEmpty()) {
            CollectedLogBook deviceLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
            deviceLogBook.setMeterEvents(meterEvents);
            getCollectedDatas().add(deviceLogBook);
        }
    }
}