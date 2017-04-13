/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.rest.util.JsonInstantAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

public class DeviceCommandInfo {
    public String command;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant activationDate;
    public String keyAccessorType;
    public String callbackSuccess;
    public String callbackError;

}