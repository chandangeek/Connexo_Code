package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.engine.model.ComServer;

import com.elster.jupiter.rest.util.JsonInstantAdapter;

import java.time.Instant;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 10/14/14.
 */
public class JournalEntryInfo {
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant timestamp;
    public String details;
    public String errorDetails;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel logLevel;
}
