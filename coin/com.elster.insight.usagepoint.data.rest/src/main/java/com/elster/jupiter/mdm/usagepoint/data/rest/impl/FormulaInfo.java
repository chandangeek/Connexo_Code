package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormulaInfo {
    public String description;

    public FormulaInfo() {
    }

    public static FormulaInfo asInfo(String description) {
        FormulaInfo info = new FormulaInfo();
        info.description = description;
        return info;
    }
}