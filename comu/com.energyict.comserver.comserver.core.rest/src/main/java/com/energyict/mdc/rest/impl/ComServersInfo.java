package com.energyict.mdc.rest.impl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ComServersInfo {
    public List<ComServerInfo> comServers = new ArrayList<>();
}
