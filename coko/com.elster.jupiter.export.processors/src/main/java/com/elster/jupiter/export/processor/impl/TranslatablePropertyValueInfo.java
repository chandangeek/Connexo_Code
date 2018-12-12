/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.properties.HasIdAndName;

public class TranslatablePropertyValueInfo extends HasIdAndName {

    private String id;
    private String name;

    public TranslatablePropertyValueInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}