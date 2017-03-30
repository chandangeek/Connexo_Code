/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.protocol.api.TrackingCategory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by bvn on 3/17/16.
 */
public class TrackingCategoryAdapter extends XmlAdapter<String, TrackingCategory> {
    @Override
    public TrackingCategory unmarshal(String v) throws Exception {
        return TrackingCategory.fromKey(v).orElse(TrackingCategory.manual);
    }

    @Override
    public String marshal(TrackingCategory v) throws Exception {
        return v != null ? v.getKey() : null;
    }

}
