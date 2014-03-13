package com.energyict.protocols.mdc.inbound.general.frames;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LogBook;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
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
        BaseDevice device = this.getDevice();
        LogBook genericLogBook = this.findGenericLogBook(device);
        if (!device.getLogBooks().isEmpty()) {
            logBookIdentifier = new LogBookIdentifierById(genericLogBook.getId());
        } else {
            getCollectedDatas().add(this.getCollectedDataFactory().createNoLogBookCollectedData(new DeviceIdentifierBySerialNumber(getInboundParameters().getSerialNumber())));
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
            CollectedLogBook deviceLogBook = this.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
            deviceLogBook.setMeterEvents(meterEvents);
            getCollectedDatas().add(deviceLogBook);
        }
    }

    protected LogBook findGenericLogBook(BaseDevice device) {
        List<LogBookFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LogBookFactory.class);
        for (LogBookFactory factory : factories) {
            LogBook genericLogBook = factory.findGenericLogBook(device);
            if (genericLogBook != null) {
                return genericLogBook;
            }
        }
        return null;
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