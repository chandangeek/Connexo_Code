package com.energyict.mdc.rest.impl.properties.propertycontexts;

import com.energyict.mdc.rest.impl.properties.PropertyContext;
import com.energyict.mdw.core.TimeZoneInUse;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:45
 */
@XmlRootElement
public class TimeZoneInUseInfo implements PropertyContext {

    public TimeZone timeZone;

    public TimeZoneInUseInfo(TimeZoneInUse timeZoneInUse) {
        this.timeZone = timeZoneInUse.getTimeZone();
    }


}
