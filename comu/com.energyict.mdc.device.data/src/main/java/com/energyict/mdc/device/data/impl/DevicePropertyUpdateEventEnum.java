/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import java.util.stream.Stream;

enum DevicePropertyUpdateEventEnum {

    IPv6Address("IPv6Address", UpdateEventType.IPADDRESSV6);

    DevicePropertyUpdateEventEnum(String name, UpdateEventType eventType) {
        this.name = name;
        this.eventType = eventType;
    }

    private final String name;
    private final UpdateEventType eventType;

    String getName(){
        return name;
    }

    String getTopic(){
        return eventType.topic();
    }

    static Stream<DevicePropertyUpdateEventEnum> stream() {
        return Stream.of(DevicePropertyUpdateEventEnum.values());
    }
}
