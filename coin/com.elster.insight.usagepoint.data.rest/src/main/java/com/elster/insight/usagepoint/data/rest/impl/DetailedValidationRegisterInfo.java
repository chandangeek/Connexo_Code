package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

/**
 * Created by adrianlupan on 4/24/15.
 */
public class DetailedValidationRegisterInfo {

    public Long id;
    public String name;
    public Long total;

    public DetailedValidationRegisterInfo(Channel register, Long count) {
        this.id = register.getId();
        this.name = register.getMainReadingType().getFullAliasName();
        this.total = count;
    }

    public DetailedValidationRegisterInfo() {

    }

}
