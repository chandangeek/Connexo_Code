/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;


/**
 * Check if Reading Type MRID is suitable for creating a Register Type
 */
final class MridDbMatcher {

    // this will select only register used for instantaneous reads :"^[08]\\.\\d+\\.0";
    // this will select registers for instantanous reads and the ones used for load profile capture objects
    private static final String BILLING_PROFILE_OR_NORMAL_REGISTERS = "^[08]\\.\\d+\\.\\d{1,2}\\.";

    private MridDbMatcher(){}

    static Condition getFilterCondition(String dbSearchText) {
        if (dbSearchText == null || dbSearchText.isEmpty()){
            return mridMatchOfRegisters();
        }
        String regex = "*" + dbSearchText.replace(" ", "*") + "*";
        return Where.where("fullAliasName").likeIgnoreCase(regex).and(mridMatchOfRegisters());
    }

    static Condition getMRIDFilterCondition(String mRID){
        return Where.where("mRID").matches(mRID, "").and(mridMatchOfRegisters());
    }

    private static Condition mridMatchOfRegisters() {
        return Where.where("mRID").matches(BILLING_PROFILE_OR_NORMAL_REGISTERS  , "");
    }

}
