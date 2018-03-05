/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

/**
 * Check if Reading Type MRID is suitable for creating a Register Type
 */
public final class MridStringMatcher {

    private static final String BILLING_OR_NORMAL_REGISTERS = "^[08]\\.\\d+\\.0.*";

    private MridStringMatcher(){}

    public static boolean isValid(String mRID){
        return mRID.matches(BILLING_OR_NORMAL_REGISTERS);
    }
}



