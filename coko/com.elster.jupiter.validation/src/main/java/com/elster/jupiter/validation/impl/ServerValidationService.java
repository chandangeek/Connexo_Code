/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;

/**
 * Adds behavior to the {@link ValidationService} interface
 * that is reserved for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-08 (17:17)
 */
public interface ServerValidationService extends ValidationService {

    Thesaurus getThesaurus();

    DataModel dataModel();

    KpiService kpiService();

}