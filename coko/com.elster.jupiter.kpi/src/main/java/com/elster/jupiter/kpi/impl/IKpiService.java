/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.KpiService;

interface IKpiService extends KpiService {

    Vault getVault();

    RecordSpec getRecordSpec();
}
