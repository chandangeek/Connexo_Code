/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.Optional;

@ProviderType
public enum RevokeStatus {

    NOT_REVOKED(-1),
    REVOCATION_REASON_UNSPECIFIED(0),
    REVOCATION_REASON_KEYCOMPROMISE(1),
    REVOCATION_REASON_CACOMPROMISE(2),
    REVOCATION_REASON_AFFILIATIONCHANGED(3),
    REVOCATION_REASON_SUPERSEDED(4),
    REVOCATION_REASON_CESSATIONOFOPERATION(5),
    REVOCATION_REASON_CERTIFICATEHOLD(6), // Value 7 is not used, see RFC5280
    REVOCATION_REASON_REMOVEFROMCRL(8),
    REVOCATION_REASON_PRIVILEGESWITHDRAWN(9),
    REVOCATION_REASON_AACOMPROMISE(10);

    private int val;

    RevokeStatus(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static Optional<RevokeStatus> fromValue(BigDecimal value) {
        return value == null ? Optional.empty() : fromValue(value.intValue());
    }

    public static Optional<RevokeStatus> fromValue(int value) {
        for (RevokeStatus action : values()) {
            if (value == action.getVal()) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }
}