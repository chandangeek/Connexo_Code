package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class SelectorInfos {

    public int total;
    public List<SelectorInfo> selectors = new ArrayList<>();

    public SelectorInfos() {
    }

    public SelectorInfo add(String name, String displayName, List<PropertyInfo> properties, SelectorType selectorType) {
        SelectorInfo result = new SelectorInfo(name, displayName, properties, selectorType);
        selectors.add(result);
        total++;
        return result;
    }
}
