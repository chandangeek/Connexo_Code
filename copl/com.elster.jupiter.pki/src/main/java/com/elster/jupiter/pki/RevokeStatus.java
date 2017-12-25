/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import java.math.BigDecimal;

public enum RevokeStatus {
    NOT_REVOKED(-1), REVOCATION_REASON_UNSPECIFIED(0), REVOCATION_REASON_KEYCOMPROMISE(1), REVOCATION_REASON_CACOMPROMISE(
            2), REVOCATION_REASON_AFFILIATIONCHANGED(3), REVOCATION_REASON_SUPERSEDED(4), REVOCATION_REASON_CESSATIONOFOPERATION(
            5), REVOCATION_REASON_CERTIFICATEHOLD(6), // Value 7 is not used, see RFC5280
    REVOCATION_REASON_REMOVEFROMCRL(8), REVOCATION_REASON_PRIVILEGESWITHDRAWN(9), REVOCATION_REASON_AACOMPROMISE(10);

    private int val;
    public static final String REVOKE_STATUS = "RevokeStatus";

    RevokeStatus(int val) {
        this.val = val;
    }

    int getVal() {
        return val;
    }

    public static RevokeStatus fromValue(BigDecimal value) throws EnumLookupValueException {
        return fromValue(value.intValue());
    }

    public static RevokeStatus fromValue(int value) throws EnumLookupValueException {
        for (RevokeStatus action : values()) {
            if (value == action.getVal()) {
                return action;
            }
        }
        return null;
    }

}
