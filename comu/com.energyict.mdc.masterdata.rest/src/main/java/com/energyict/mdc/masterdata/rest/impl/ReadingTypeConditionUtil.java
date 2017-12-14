package com.energyict.mdc.masterdata.rest.impl;


import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;


class ReadingTypeConditionUtil {

    static Condition searchTextMatch(String dbSearchText) {
        String regex = "*" + dbSearchText.replace(" ", "*") + "*";
        return Where.where("fullAliasName").likeIgnoreCase(regex)
                .and(mrIdMatchOfNormalRegisters()
                        .or(mrIdMatchOfBillingRegisters())
                        .or(mrIdMatchOfPeriodRelatedRegisters()));
    }

    static Condition mridFromObisMatch(String mRID) {
        return Where.where("mRID").matches(mRID, "")
                .and(mrIdMatchOfNormalRegisters()
                        .or(mrIdMatchOfBillingRegisters())
                        .or(mrIdMatchOfPeriodRelatedRegisters()));
    }

    private static Condition mrIdMatchOfPeriodRelatedRegisters() {
        return Where.where("mRID").matches("^[11-13]\\.\\[1-24]\\.0", "");
    }

    private static Condition mrIdMatchOfBillingRegisters() {
        return Where.where("mRID").matches("^8\\.\\d+\\.0", "");
    }

    private static Condition mrIdMatchOfNormalRegisters() {
        return Where.where("mRID").matches("^0\\.\\d+\\.0", "");
    }

}
