/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.ami;

import java.math.BigDecimal;

public class ChangeTaxRatesInfo {
    public BigDecimal monthlyTax;
    public BigDecimal zeroConsumptionTax;
    public BigDecimal consumptionTax;
    public BigDecimal consumptionAmount;
    public BigDecimal consumptionLimit;
}
