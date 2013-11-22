package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.impl.properties.MdcResourceProperty;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.mdw.coreimpl.TimeZoneInUseFactoryImpl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:45
 */
@XmlRootElement
public class TimeZoneInUseInfo implements MdcResourceProperty {

    public TimeZone timeZone;

    public TimeZoneInUseInfo(TimeZoneInUse timeZoneInUse) {
        this.timeZone = timeZoneInUse.getTimeZone();
    }


    @Override
    public Object fromResourceObject() {
        return new TimeZoneInUseFactoryImpl().find(timeZone);
    }
}
