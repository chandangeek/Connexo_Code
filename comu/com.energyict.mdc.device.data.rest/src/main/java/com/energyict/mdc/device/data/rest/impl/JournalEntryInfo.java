package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.engine.model.ComServer;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 10/14/14.
 */
public class JournalEntryInfo {
    public Date timestamp;
    public String details;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel logLevel;
}
