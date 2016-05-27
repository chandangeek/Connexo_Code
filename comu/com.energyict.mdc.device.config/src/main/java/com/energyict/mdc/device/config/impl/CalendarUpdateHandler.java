package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;


@Component(name = "com.energyict.mdc.device.config.calendar.updateHandler", service = TopicHandler.class,immediate = true)
public class CalendarUpdateHandler implements TopicHandler {

    public CalendarUpdateHandler() {
    }

    @Override
    public void handle(LocalEvent localEvent) {

        Calendar calendar = (Calendar) localEvent.getSource();
    }



    @Override
    public String getTopicMatcher() {
        return EventType.CALENDAR_UPDATE.topic();
    }


}



