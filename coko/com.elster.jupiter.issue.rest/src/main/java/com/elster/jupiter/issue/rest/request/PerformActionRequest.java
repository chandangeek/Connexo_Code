package com.elster.jupiter.issue.rest.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformActionRequest {
    
    private long id;
    
    private Map<String, String> parameters = new HashMap<String, String>();
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
        
    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
}
