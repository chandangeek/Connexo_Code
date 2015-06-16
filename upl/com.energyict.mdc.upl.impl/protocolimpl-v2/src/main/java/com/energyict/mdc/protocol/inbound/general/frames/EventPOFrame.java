package com.energyict.mdc.protocol.inbound.general.frames;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdw.core.LogBookTypeFactory;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

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

    public EventPOFrame(String frame, CallHomeIdPlaceHolder callHomeIdPlaceHolder) {
        super(frame, callHomeIdPlaceHolder);
    }

    @Override
    public void doParse() {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        LogBookIdentifier logBookIdentifier = new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifierByDialHomeIdPlaceHolder(), LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE);

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
            deviceLogBook.setCollectedMeterEvents(meterEvents);
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