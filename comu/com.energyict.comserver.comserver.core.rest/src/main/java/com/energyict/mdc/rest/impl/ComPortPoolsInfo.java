package com.energyict.mdc.rest.impl;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ComPortPoolsInfo {
    @XmlElement
    @XmlElementWrapper(name = "ComPortPools")
    public Collection<ComPortPoolInfo> comPortPools = new ArrayList<>();
}
