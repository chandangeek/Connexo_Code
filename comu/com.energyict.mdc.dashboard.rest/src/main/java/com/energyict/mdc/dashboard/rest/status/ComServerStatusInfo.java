/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.rest.TimeDurationInfo;

import java.time.Instant;

/**
 * Represents the status of a ComServer in the REST layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (09:23)
 */
public class ComServerStatusInfo {

    public String comServerName;
    public long comServerId;
    public String comServerType;
    public boolean running;
    public boolean blocked;
    public TimeDurationInfo blockTime;
    public Instant blockedSince;
    public String uri;

}