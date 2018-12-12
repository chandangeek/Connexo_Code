/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LongAdapter extends XmlAdapter<String, Long> {

    @Override
    public Long unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return Long.valueOf(jsonValue);
    }

    @Override
    public String marshal(Long value) throws Exception {
        if (value==null) {
            return null;
        }
        return value.toString();
    }
}
