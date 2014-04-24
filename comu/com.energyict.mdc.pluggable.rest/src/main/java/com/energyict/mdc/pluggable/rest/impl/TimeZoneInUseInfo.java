package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the Info object for a {@link TimeZoneInUse}
 *
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:45
 */
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
