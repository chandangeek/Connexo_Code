/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class JsonInstantAdapter extends XmlAdapter<Long, Instant> {

    @Override
    public Instant unmarshal(Long v) throws Exception {
        return v == null ? null : Instant.ofEpochSecond(v.longValue());
    }

    @Override
    public Long marshal(Instant v) throws Exception {
        return v == null ? null : v.getEpochSecond();
    }

}