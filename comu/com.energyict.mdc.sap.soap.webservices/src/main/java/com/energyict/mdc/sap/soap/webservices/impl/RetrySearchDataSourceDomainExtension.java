/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.servicecall.ServiceCall;

import java.math.BigDecimal;

public interface RetrySearchDataSourceDomainExtension extends PersistentDomainExtension<ServiceCall> {
    BigDecimal getAttemptNumber();

    void setAttemptNumber(BigDecimal attemptNumber);

    ServiceCall getServiceCall();
}
