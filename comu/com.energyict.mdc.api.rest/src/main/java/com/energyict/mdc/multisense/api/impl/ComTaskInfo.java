package com.energyict.mdc.multisense.api.impl;

import java.util.List;

/**
 * Created by bvn on 7/17/15.
 */
public class ComTaskInfo extends LinkInfo<Long> {
    public String name;
    public List<LinkInfo> categories;
    public List<LinkInfo> commands;
}
