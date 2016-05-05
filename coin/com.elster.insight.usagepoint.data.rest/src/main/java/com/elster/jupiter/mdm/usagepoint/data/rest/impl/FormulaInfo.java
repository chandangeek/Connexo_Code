package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.config.Formula;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormulaInfo {
    public String description;

    public FormulaInfo() {
    }

    public static FormulaInfo asInfo(Formula formula) {
        FormulaInfo info = new FormulaInfo();
        info.description = formula.getDescription();
        return info;
    }
}
