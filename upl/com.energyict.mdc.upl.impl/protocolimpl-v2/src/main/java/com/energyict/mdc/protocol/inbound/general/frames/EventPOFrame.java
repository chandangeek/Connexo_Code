package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LogBook;
import com.energyict.mdw.core.LogBookFactoryProvider;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierById;
import com.energyict.protocolimplv2.identifiers.SerialNumberPlaceHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Override
    protected FrameType getType() {
        return FrameType.EVENTP0;
    }

    public EventPOFrame(String frame, SerialNumberPlaceHolder serialNumberPlaceHolder) {
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
                    Date timeStamp = parseTimeStampString(nameAndEvent[1]);
                    meterEvents.add(
                            new MeterProtocolEvent(timeStamp,
                                    MeterEvent.POWERDOWN,
                                    UNKNOWN,
                                    EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(MeterEvent.POWERDOWN),
                                    "Last gas power outage",
                                    UNKNOWN,
                                    UNKNOWN)
                    );
                }
            }
        }
        if (!meterEvents.isEmpty()) {
            CollectedLogBook deviceLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
            deviceLogBook.setMeterEvents(meterEvents);
            getCollectedDatas().add(deviceLogBook);
        }
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