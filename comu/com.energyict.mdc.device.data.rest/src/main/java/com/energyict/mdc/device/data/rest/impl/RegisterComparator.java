package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Register;

import java.util.Comparator;

public class RegisterComparator implements Comparator<Register> {

    @Override
    public int compare(Register o1, Register o2) {
        return o1.getRegisterSpec().getRegisterMapping().getName().compareToIgnoreCase(o2.getRegisterSpec().getRegisterMapping().getName());
    }
}