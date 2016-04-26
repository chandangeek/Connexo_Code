package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormulaInfo {
    public String description;

    public FormulaInfo() {
    }
}
