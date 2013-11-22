package com.energyict.mdc.rest.impl;

import com.energyict.mdw.core.TimeZoneInUse;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 10:45
 */
@XmlRootElement
public class TimeZoneInUseInfo {

    public TimeZone timeZone;

    public TimeZoneInUseInfo(TimeZoneInUse timeZoneInUse) {
        this.timeZone = timeZoneInUse.getTimeZone();
    }


}
