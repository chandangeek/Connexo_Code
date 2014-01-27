package com.energyict.mdc.rest.impl.comserver;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;


@XmlRootElement
public class ComPortPoolsInfo {
    @JsonProperty("comPortPools")
    public Collection<ComPortPoolInfo> comPortPools = new ArrayList<>();
}
