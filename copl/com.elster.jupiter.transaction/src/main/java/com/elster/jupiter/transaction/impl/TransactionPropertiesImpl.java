/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.transaction.TransactionProperties;

import java.util.HashMap;
import java.util.Map;

public class TransactionPropertiesImpl implements TransactionProperties {

    private Map<String, Object> properties = new HashMap<>();

    @Override
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public void removeProperty(String name) {
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }
}
