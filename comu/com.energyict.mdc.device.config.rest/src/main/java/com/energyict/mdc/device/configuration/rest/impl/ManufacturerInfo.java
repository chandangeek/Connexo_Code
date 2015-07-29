package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.Manufacturer;
import com.energyict.mdc.protocol.api.Model;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ManufacturerInfo {
    @JsonProperty("manufacturerName")
    public String name;
    @JsonProperty("models")
    public List<String> modelNames;

    public ManufacturerInfo(Manufacturer manufacturer) {
        this.name = manufacturer.name(); // TODO convert using wrapper to shield JS from changes
        modelNames = new ArrayList<>();
        for (Model model : manufacturer.getModels()) {
            modelNames.add(model.getName()); // TODO convert using wrapper to shield JS from changes
        }
    }


}
