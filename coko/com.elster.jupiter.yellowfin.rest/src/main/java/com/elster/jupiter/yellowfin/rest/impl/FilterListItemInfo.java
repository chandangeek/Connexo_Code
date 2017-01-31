/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.YellowfinFilterListItemInfo;
import com.elster.jupiter.yellowfin.YellowfinReportInfo;

/**
 * Created by Albertv on 12/3/2014.
 */
public class FilterListItemInfo {

    private String value1;
    private String value2;

    public FilterListItemInfo(YellowfinFilterListItemInfo reportInfo){
        this.value1 = reportInfo.getValue1();
        this.value2 = reportInfo.getValue2();
    }
    public String getValue1() {
        return value1;
    }

    public void setValue1(String value2) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {

        this.value2 = value2;
    }
}
