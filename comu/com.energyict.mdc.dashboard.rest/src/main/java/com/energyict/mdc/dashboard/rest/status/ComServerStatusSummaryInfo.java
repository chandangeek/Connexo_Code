/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.comserver.ComServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the summary of statusses of all {@link ComServer}s
 * that are configured in the system, in the REST layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (16:35)
 */
public class ComServerStatusSummaryInfo {

    public List<ComServerStatusInfo> comServerStatusInfos = new ArrayList<>();

}