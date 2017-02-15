/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.model.DataQualityKpi', {
    extend: 'Uni.model.Version',
    fields: ['id', 'usagePointGroup', 'frequency', 'latestCalculationDate', 'purposes'],
    proxy: {
        type: 'rest',
        url: '/api/val/kpis'
    }
});