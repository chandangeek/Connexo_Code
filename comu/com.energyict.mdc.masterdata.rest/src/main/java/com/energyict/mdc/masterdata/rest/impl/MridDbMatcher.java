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

    private static final String PERIOD_RELATED_REGISTERS ="^(11|13)\\.([1-9]|1[0-9]|2[0-4])\\.0";
    private static final String BILLING_OR_NORMAL_REGISTERS = "^[08]\\.\\d+\\.0";

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
        return mrIdMatchOfBillingOrNormalRegisters()
                .or(mrIdMatchOfPeriodRelatedRegisters());
    }

    private static Condition mrIdMatchOfPeriodRelatedRegisters() {
        return Where.where("mRID").matches(PERIOD_RELATED_REGISTERS, "");
    }

    private static Condition mrIdMatchOfBillingOrNormalRegisters() {
        return Where.where("mRID").matches(BILLING_OR_NORMAL_REGISTERS, "");
    }

}
