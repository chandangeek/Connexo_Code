package com.elster.jupiter.export;


import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.util.function.Predicate;

public interface EndDeviceEventTypeFilter {

    Predicate<EndDeviceEventType> asEndDeviceEventTypePredicate();

    Predicate<String> asEndDeviceEventTypeCodePredicate();

    default Predicate<EndDeviceEvent> asEndDeviceEventPredicate() {
        return event -> asEndDeviceEventTypeCodePredicate().test(event.getType());
    }

    String getCode();
}
