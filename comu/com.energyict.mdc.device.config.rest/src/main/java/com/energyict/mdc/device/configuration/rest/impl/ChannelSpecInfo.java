package com.energyict.mdc.device.configuration.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ChannelSpecInfo {
    public long id;
    public String name;
    public boolean useMultiplier;
}
