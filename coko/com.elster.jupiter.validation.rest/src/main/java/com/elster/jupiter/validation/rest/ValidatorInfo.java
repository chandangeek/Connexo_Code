package com.elster.jupiter.validation.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ValidatorInfo {

    public String implementation;
    public String displayName;

    public ValidatorInfo(String implementation, String displayName) {
        this.implementation = implementation;
        this.displayName = displayName;
    }

    public ValidatorInfo() {
    }





}
