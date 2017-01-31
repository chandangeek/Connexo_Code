/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

/**
 * Created by bvn on 7/17/15.
 */
public class ComTaskInfo extends LinkInfo<Long> {
    public String name;
    public List<LinkInfo> categories;
    public List<LinkInfo> commands;
}
