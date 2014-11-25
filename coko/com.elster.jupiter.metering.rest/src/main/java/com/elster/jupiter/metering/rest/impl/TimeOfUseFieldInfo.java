package com.elster.jupiter.metering.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TimeOfUseFieldInfo {
    public String name;
    public int tou;

    public TimeOfUseFieldInfo() {
    }

    public TimeOfUseFieldInfo(int total) {
        this.name = String.valueOf(total);
        this.tou = total;
    }
}