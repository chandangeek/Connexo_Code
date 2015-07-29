package com.energyict.mdc.device.configuration.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * ListWrapper object to mitigate info to JS side. Could be extended to allow paging, not required at this point.
 */
@XmlRootElement
public class ManufacturerInfos {
    @JsonProperty("manufacturers")
    public List<ManufacturerInfo> manufacturerInfos;
}
