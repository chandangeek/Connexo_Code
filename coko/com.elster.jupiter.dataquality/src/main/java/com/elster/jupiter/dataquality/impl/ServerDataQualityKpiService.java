/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.orm.DataModel;

public interface ServerDataQualityKpiService extends DataQualityKpiService {

    DataModel getDataModel();

}
