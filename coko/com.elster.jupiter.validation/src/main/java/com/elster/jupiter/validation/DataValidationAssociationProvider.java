package com.elster.jupiter.validation;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Created by albertv on 6/29/2016.
 */
public interface DataValidationAssociationProvider {

    BigDecimal getRegisterSuspects(String mRID, Range<Instant> range);

    BigDecimal getChannelsSuspects(String mRID, Range<Instant> range);
}
