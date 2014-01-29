package com.energyict.mdc.rest.impl.comserver;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonPropertyOrder({"total","comServers"})
public class ComServersInfo {
    @JsonIgnore
    private int couldHaveNextPage = 0;

    @JsonProperty("comServers")
    public List<ComServerInfo> comServers = new ArrayList<>();

    @JsonProperty("total")
    public int getTotal() {
        return comServers.size() + couldHaveNextPage;
    }

    public void setCouldHaveNextPage(){
        couldHaveNextPage = 1;
    }
}
