package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;

import java.util.List;

/**
 * Created by bvn on 7/20/15.
 */
public class DeviceMessageCategoryInfo extends LinkInfo<Long> {
    public String name;
    public String description;
    public List<LinkInfo> deviceMessageSpecs;
}
