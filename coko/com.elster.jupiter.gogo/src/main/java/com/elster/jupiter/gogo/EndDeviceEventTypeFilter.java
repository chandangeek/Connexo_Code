/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.gogo;


import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.events.EndDeviceEventType;

public class EndDeviceEventTypeFilter {
    private final boolean allApplicable;
    private final EndDeviceType type;
    private final EndDeviceDomain domain;
    private final EndDeviceSubDomain subDomain;
    private final EndDeviceEventOrAction eventorAction;

    public EndDeviceEventTypeFilter(String... filter) {
        if (filter.length == 0) {
            allApplicable = true;
            type = null;
            domain = null;
            subDomain = null;
            eventorAction = null;
        } else if (filter.length == 4) {
            allApplicable = false;
            type = EndDeviceType.get(getFilterNumber(filter[0]));
            domain = EndDeviceDomain.get(getFilterNumber(filter[1]));
            subDomain = EndDeviceSubDomain.get(getFilterNumber(filter[2]));
            eventorAction = EndDeviceEventOrAction.get(getFilterNumber(filter[3]));
        } else {
            throw new IllegalArgumentException("Filter must contain 4 values");
        }
    }

    private int getFilterNumber(String s) {
        if ("*".equals(s)) {
            return 0;
        } else {
            return Integer.parseInt(s);
        }
    }

    public boolean isApplicable(EndDeviceEventType endDeviceEventType) {
        if (allApplicable) {
            return true;
        } else {
            return isApplicable(endDeviceEventType.getType(), type)
                    && isApplicable(endDeviceEventType.getDomain(), domain)
                    && isApplicable(endDeviceEventType.getSubDomain(), subDomain)
                    && isApplicable(endDeviceEventType.getEventOrAction(), eventorAction);
        }
    }

    private boolean isApplicable(Enum type, Enum filter) {
        return filter.ordinal()==0 || type.equals(filter);
    }
}