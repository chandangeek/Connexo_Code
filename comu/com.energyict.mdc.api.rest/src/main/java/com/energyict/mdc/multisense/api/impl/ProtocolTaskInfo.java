/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

/**
 * Created by bvn on 7/20/15.
 */
public class ProtocolTaskInfo extends LinkInfo<Long> {
    public String category;
    public String action;
}
