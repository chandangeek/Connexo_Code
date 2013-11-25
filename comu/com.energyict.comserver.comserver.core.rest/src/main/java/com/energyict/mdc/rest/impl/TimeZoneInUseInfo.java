package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.impl.properties.MdcResourceProperty;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.mdw.coreimpl.TimeZoneInUseFactoryImpl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.TimeZone;

/**
 * Represents the Info object for a {@link TimeZoneInUse}
 *
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:45
 */
@XmlRootElement
public class TimeZoneInUseInfo implements MdcResourceProperty {

    public TimeZone timeZone;

    public TimeZoneInUseInfo() {
    }

    public TimeZoneInUseInfo(Map<String, Object> map){
        this.timeZone = TimeZone.getTimeZone((String) map.get("timeZone"));
    }

    public TimeZoneInUseInfo(TimeZoneInUse timeZoneInUse) {
        this.timeZone = timeZoneInUse.getTimeZone();
    }


    @Override
    public Object fromInfoObject() {
        return new TimeZoneInUseFactoryImpl().find(timeZone);
    }
}
