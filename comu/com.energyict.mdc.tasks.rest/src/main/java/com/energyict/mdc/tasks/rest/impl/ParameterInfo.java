/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterInfo {
    public static final String NAME = "name";
    public static final String VALUE = "value";

    public String name;
    public Object value;

    public ParameterInfo() {
    }

    public ParameterInfo(String name) {
        this.name = name;
    }

    public static ParameterInfo from(Map<String, Object> map) {
        ParameterInfo parameterInfo = new ParameterInfo();
        parameterInfo.name = (String) map.get(NAME);
        parameterInfo.value = map.get(VALUE);
        return parameterInfo;
    }

    public static List<ParameterInfo> from(List<Map<String, Object>> list) {
        List<ParameterInfo> parameterInfos = new ArrayList<>(list.size());
        for (Map<String, Object> map : list) {
            parameterInfos.add(ParameterInfo.from(map));
        }
        return parameterInfos;
    }
}