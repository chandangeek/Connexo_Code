package com.elster.jupiter.prepayment.impl;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by bvn on 9/18/15.
 */
public class ContactorInfo {

    public Status status;
    public Instant activationDate;
    public BigDecimal loadLimit;
    public Integer loadTolerance;
    public Integer[] tariffs;
    public String readingType;
    public String callback;
}
