package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Contains a non-limited, non-paged list of the history of linked datalogger-slave registers
 */
@XmlRootElement
public class RegisterHistoryInfos {
    @XmlElement
    public Collection<RegisterHistoryInfo> registerHistory = new HashSet<>();
}
