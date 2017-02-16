/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.inject.Inject;
import java.lang.reflect.Field;

public class LocationShortInfoFactory {

    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public LocationShortInfoFactory(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public LocationShortInfo asInfo(Location location) {
        LocationShortInfo info = new LocationShortInfo();
        StringBuilder sb = new StringBuilder();
        location.getMember(threadPrincipalService.getLocale().toString()).ifPresent(locationMember -> {
                    //Add serializer on location class
                    Class<?> objClass = locationMember.getClass();
                    Field[] fields = objClass.getFields();
                    for (Field field : fields) {
                        try {
                            sb.append(field.get(locationMember).toString()).append(",");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        info.location = sb.toString();

        return info;
    }
}
