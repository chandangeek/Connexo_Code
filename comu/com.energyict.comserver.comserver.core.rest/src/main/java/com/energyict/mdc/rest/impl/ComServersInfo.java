package com.energyict.mdc.rest.impl;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ComServersInfo {
    @XmlElement
    @XmlElementWrapper(name = "ComServers")
    public Collection<? super ComServerInfo> comServers = new HashSet<>();
}
