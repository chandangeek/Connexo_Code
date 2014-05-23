package com.energyict.mdc.tasks.rest.impl.infos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterInfo {
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private String name;
    private Object value;

    public ParameterInfo() {
    }

    public ParameterInfo(String name) {
        this.name = name;
    }

    public static ParameterInfo from(Map<String, Object> map) {
        ParameterInfo parameterInfo = new ParameterInfo();
        parameterInfo.setName((String) map.get(NAME));
        parameterInfo.setValue(map.get(VALUE));
        return parameterInfo;
    }

    public static List<ParameterInfo> from(List<Map<String, Object>> list) {
        List<ParameterInfo> parameterInfos = new ArrayList<>(list.size());
        for (Map<String, Object> map : list) {
            parameterInfos.add(ParameterInfo.from(map));
        }
        return parameterInfos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}