/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointEstimation;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;

public class UsagePointServiceImpl implements UsagePointService {

    private final DataModel dataModel;

    public UsagePointServiceImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public UsagePointValidation forValidation(UsagePoint usagePoint) {
        return dataModel.getInstance(UsagePointValidationImpl.class).init(usagePoint);
    }

    @Override
    public UsagePointEstimation forEstimation(UsagePoint usagePoint) {
        return dataModel.getInstance(UsagePointEstimationImpl.class).init(usagePoint);
    }
}
