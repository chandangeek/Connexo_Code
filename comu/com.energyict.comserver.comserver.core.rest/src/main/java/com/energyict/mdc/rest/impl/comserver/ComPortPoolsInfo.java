package com.energyict.mdc.rest.impl.comserver;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;


@XmlRootElement
@JsonPropertyOrder({"total","comPortPools"})
public class ComPortPoolsInfo {
    @JsonIgnore
    private int couldHaveNextPage = 0;

    @JsonProperty("comPortPools")
    public Collection<ComPortPoolInfo> comPortPools = new ArrayList<>();

    @JsonProperty("total")
    public int getTotal() {
        return comPortPools.size() + couldHaveNextPage;
    }

    public void setCouldHaveNextPage(){
        couldHaveNextPage = 1;
    }
}
