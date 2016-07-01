package com.elster.jupiter.validation;

import java.math.BigDecimal;

/**
 * Created by albertv on 6/29/2016.
 */
public interface DataValidationAssociationProvider {

    BigDecimal getRegisterSuspects(String mRID);

    BigDecimal getChannelsSuspects(String mRID);
}
