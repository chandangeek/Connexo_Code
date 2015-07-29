package com.elster.jupiter.issue.rest.request;


import java.util.List;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformActionRequest {
    
    public long id;
    public List<PropertyInfo> properties;
}
