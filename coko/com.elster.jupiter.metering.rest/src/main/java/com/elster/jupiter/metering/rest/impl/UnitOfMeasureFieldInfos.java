package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XmlRootElement
public class UnitOfMeasureFieldInfos {
    public int total;
    public List<UnitOfMeasureFieldInfo> unitsOfMeasure = new ArrayList<>();

    public void add(HashMap<String, ReadingType> map) {
        for (String name : map.keySet()) {
            ReadingType rt = map.get(name);
            int multiplier = rt.getMultiplier().getMultiplier();
            int unit = rt.getUnit().getId();
            unitsOfMeasure.add(new UnitOfMeasureFieldInfo(name, multiplier, unit));
            total++;
        }
    }
}