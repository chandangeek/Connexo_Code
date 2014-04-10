package com.energyict.mdc.pluggable.rest.impl;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 15:53
 */
@XmlRootElement
public class TimeZoneInUseInfos {

    @XmlElement
    @XmlElementWrapper(name = "TimeZoneInUse")
    public Collection<? super TimeZoneInUseInfo> timeZonesInUse = new HashSet<>();
}
