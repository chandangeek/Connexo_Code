package com.energyict.mdc.pluggable.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:14
 */
@XmlRootElement
public class CalendarInfos {

    @XmlElement
    @XmlElementWrapper(name = "Code")
    public Collection<? super CalendarInfo> codeTableInfos = new HashSet<>();

}