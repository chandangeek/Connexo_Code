package com.elster.jupiter.metering.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EndDeviceEventTypePartInfo {
    public String name;
    public String mnemonic;
    public Integer value;
    public String displayName;
}
