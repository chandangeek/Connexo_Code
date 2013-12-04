package com.energyict.mdc.rest.impl.filter;


import org.codehaus.jackson.annotate.JsonProperty;

public class FilteredField {
    @JsonProperty("value")
    public String value;
   	@JsonProperty("property")
    public String property;
}
