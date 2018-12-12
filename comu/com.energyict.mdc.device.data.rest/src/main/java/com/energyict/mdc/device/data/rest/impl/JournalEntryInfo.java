/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonInstantAdapter;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.engine.config.ComServer;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

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
