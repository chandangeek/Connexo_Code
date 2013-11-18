package com.energyict.mdc.rest.impl;

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ComPortFilter {
    @JsonProperty("filter") public List<Map<String, String>> filter;

}
