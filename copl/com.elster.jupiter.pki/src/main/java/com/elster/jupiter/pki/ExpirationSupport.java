package com.elster.jupiter.pki;

import com.elster.jupiter.properties.Expiration;
import com.elster.jupiter.util.conditions.Comparison;

import java.time.Instant;
import java.util.List;

/**
 * Support for checking expiration
 * T type of SecurityValueWrapper
 * Date: 11/10/2017
 * Time: 11:51
 */
public interface ExpirationSupport {

    List<SecurityValueWrapper> findExpired(Expiration expiration, Instant when);

    Comparison isExpiredCondition(Expiration expiration, Instant when);
}
