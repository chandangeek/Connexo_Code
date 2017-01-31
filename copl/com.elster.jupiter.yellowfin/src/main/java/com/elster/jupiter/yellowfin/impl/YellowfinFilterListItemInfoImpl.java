/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterListItemInfo;

/**
 * Created by Albertv on 12/5/2014.
 */
public class YellowfinFilterListItemInfoImpl implements YellowfinFilterListItemInfo {
    private String value1;
    private String value2;
    @Override
    public String getValue1() {
        return value1;
    }

    @Override
    public void setValue1(String value1) {
        this.value1 = value1;
    }

    @Override
    public String getValue2() {
        return value2;
    }

    @Override
    public void setValue2(String value2) {
        this.value2 = value2;
    }
}

