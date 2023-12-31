/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.device.config.SecurityPropertySet;

import java.util.Comparator;

public class SecurityPropertySetComparator implements Comparator<SecurityPropertySet> {

    @Override
    public int compare(SecurityPropertySet o1, SecurityPropertySet o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}