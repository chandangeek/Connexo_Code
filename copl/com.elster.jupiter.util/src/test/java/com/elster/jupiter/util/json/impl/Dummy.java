/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json.impl;

import java.util.Properties;

public class Dummy {

    private Properties properties = new Properties();

    private Dummy() {}

    public Dummy(Properties properties) {
        this.properties.putAll(properties);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.putAll(this.properties);
        return properties;
    }
}
