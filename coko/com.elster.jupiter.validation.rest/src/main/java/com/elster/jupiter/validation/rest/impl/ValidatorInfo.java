package com.elster.jupiter.validation.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ValidatorInfo {

    public String implementation;

    public ValidatorInfo(String implementation) {
        this.implementation = implementation;
    }

    public ValidatorInfo() {
    }





}
