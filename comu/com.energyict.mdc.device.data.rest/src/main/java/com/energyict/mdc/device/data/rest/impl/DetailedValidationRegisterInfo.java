package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.NumericalRegister;

/**
 * Created by adrianlupan on 4/24/15.
 */
public class DetailedValidationRegisterInfo {

    public Long id;
    public String name;
    public Long total;

    public DetailedValidationRegisterInfo(NumericalRegister register, Long count) {
        this.id = register.getRegisterSpecId();
        this.name = register.getRegisterSpec().getReadingType().getFullAliasName();
        this.total = count;
    }

    public DetailedValidationRegisterInfo() {

    }

}
