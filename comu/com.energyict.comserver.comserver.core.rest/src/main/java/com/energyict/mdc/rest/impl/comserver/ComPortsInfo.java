package com.energyict.mdc.rest.impl.comserver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;


@XmlRootElement
public class ComPortsInfo {
    @XmlElement
    @XmlElementWrapper(name = "ComPorts")
    public Collection<ComPortInfo> comPorts = new ArrayList<>();
}
