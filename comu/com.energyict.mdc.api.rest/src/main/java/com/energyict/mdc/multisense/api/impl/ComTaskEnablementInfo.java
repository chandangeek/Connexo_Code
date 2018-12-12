/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class ComTaskEnablementInfo extends LinkInfo<Long> {
    public LinkInfo comTask;
    public LinkInfo securityPropertySet;
    public LinkInfo partialConnectionTask;
    public Boolean useDefaultConnectionTask;
    public LinkInfo useConnectionTaskWithConnectionFunction;
    public Integer priority;
    public Boolean suspended;
}
