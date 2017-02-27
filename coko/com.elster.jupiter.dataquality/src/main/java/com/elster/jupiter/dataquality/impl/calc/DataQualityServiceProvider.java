/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import java.time.Clock;

interface DataQualityServiceProvider {

    ThreadPrincipalService threadPrincipalService();

    TransactionService transactionService();

    DataQualityKpiService dataQualityKpiService();

    ValidationService validationService();

    EstimationService estimationService();

    DataModel dataModel();

    Clock clock();

}