package com.elster.jupiter.export.rest;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class ProcessorInfos {

    public int total;
    public List<ProcessorInfo> processors = new ArrayList<ProcessorInfo>();

    public ProcessorInfos() {
    }

    public ProcessorInfo add(String name, String displayName, List<PropertyInfo> properties) {
        ProcessorInfo result = new ProcessorInfo(name, displayName, properties);
        processors.add(result);
        total++;
        return result;
    }
}
