/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.TimeZone;

@XmlRootElement
public class TimeZoneInUseInfo  {

    public TimeZone timeZone;

    public TimeZoneInUseInfo() {
    }

    public TimeZoneInUseInfo(Map<String, Object> map){
        this.timeZone = TimeZone.getTimeZone((String) map.get("timeZone"));
    }

    public TimeZoneInUseInfo(TimeZoneInUse timeZoneInUse) {
        this.timeZone = timeZoneInUse.getTimeZone();
    }


}
