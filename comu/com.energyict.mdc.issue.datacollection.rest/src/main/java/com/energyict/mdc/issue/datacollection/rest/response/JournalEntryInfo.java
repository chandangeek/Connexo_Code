/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;

import com.energyict.mdc.engine.config.ComServer;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

public class JournalEntryInfo {
    public Instant timestamp;
    public String details;
    @XmlJavaTypeAdapter(LogLevelAdapter.class)
    public ComServer.LogLevel logLevel;
}
