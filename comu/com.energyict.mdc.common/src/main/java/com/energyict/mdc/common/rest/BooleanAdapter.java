/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanAdapter extends XmlAdapter<String, Boolean> {

    @Override
    public Boolean unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        if (jsonValue.equalsIgnoreCase("true")) {
            return true;
        } else if (jsonValue.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new IllegalArgumentException("invalid boolean value");
        }
    }

    @Override
    public String marshal(Boolean aBoolean) throws Exception {
        if (aBoolean==null) {
            return null;
        }
        return aBoolean.toString();
    }
}
