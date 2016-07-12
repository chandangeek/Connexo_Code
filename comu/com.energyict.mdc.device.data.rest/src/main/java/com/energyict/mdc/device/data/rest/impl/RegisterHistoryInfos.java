package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a non-limited, non-paged list of the history of linked datalogger-slave registers
 */
@XmlRootElement
public class RegisterHistoryInfos {
    @XmlElement
    public List<RegisterHistoryInfo> registerHistory = new ArrayList<>();
}
