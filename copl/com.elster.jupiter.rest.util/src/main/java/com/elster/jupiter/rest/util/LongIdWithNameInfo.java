/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

/**
 * Created by bvn on 12/17/14.
 */
public class LongIdWithNameInfo {
    public Long id;
    public String name;

    public LongIdWithNameInfo() {
    }

    public LongIdWithNameInfo(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
