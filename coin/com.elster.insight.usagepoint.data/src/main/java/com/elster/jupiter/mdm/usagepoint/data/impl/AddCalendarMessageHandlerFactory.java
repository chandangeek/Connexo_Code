package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.calendar.UsagePointCalendarService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.bulk.adding.calendar.message.handler.factory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + UsagePointDataModelService.BULK_HANDLING_QUEUE_SUBSCRIBER,
                "destination=" + UsagePointDataModelService.BULK_HANDLING_QUEUE_DESTINATION},
        immediate = true)
public class AddCalendarMessageHandlerFactory implements MessageHandlerFactory {

    private volatile JsonService jsonService;
    private volatile MeteringService meteringService;
    private volatile UsagePointCalendarService usagePointCalendarService;
    private volatile CalendarService calendarService;

    @Override
    public MessageHandler newMessageHandler() {
        return message -> {
            AddCalendarMessage addCalendarMessage = jsonService.deserialize(message.getPayload(), AddCalendarMessage.class);

            UsagePoint usagePoint = meteringService.findUsagePointById(addCalendarMessage.getUsagePointId()).get();
            Calendar calendar = calendarService.findCalendar(addCalendarMessage.getCalendarId()).get();
            Instant startTime = Instant.ofEpochMilli(addCalendarMessage.getStartTime());
            if(addCalendarMessage.isImmediately()){
                usagePointCalendarService.calendarsFor(usagePoint).addCalendar(calendar);
            } else {
                usagePointCalendarService.calendarsFor(usagePoint)
                        .addCalendar(startTime, calendar);
            }

        };
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUsagePointCalendarService(UsagePointCalendarService usagePointCalendarService) {
        this.usagePointCalendarService = usagePointCalendarService;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }
}
