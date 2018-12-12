/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class JsonInstantAdapter extends XmlAdapter<Long, Instant> {

    @Override
    public Instant unmarshal(Long v) throws Exception {
        return v == null ? null : Instant.ofEpochMilli(v.longValue());
    }

    @Override
    public Long marshal(Instant v) throws Exception {
        return v == null ? null : v.toEpochMilli();
    }

}