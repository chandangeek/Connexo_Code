package com.energyict.mdc.common.rest;

/**
 */
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonPropertyOrder({"total", "comServers"})
public class InfoList<T> {

    private final String COM_SERVERS = "comServers";
    @JsonIgnore
    private final String listName;
    @JsonIgnore
    private int couldHaveNextPage = 0;

//    @JsonProperty(listName)
    public List<T> infoList = new ArrayList<>();

    public InfoList(String listName) {
        this.listName = listName;
    }

    @JsonProperty("total")
    public int getTotal() {
        return infoList.size() + couldHaveNextPage;
    }

    public void setCouldHaveNextPage(){
        couldHaveNextPage = 1;
    }
}
