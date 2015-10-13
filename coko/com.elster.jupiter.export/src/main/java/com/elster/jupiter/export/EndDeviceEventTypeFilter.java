package com.elster.jupiter.export;


import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.util.function.Predicate;

public interface EndDeviceEventTypeFilter {

    Predicate<EndDeviceEventType> asEndDeviceEventTypePredicate();

    default Predicate<EndDeviceEvent> asEndDeviceEventPredicate() {
        return event -> asEndDeviceEventPredicate().test(event);
    }

    String getCode();
}
