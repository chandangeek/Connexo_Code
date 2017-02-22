/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.model.DataQualityKpi', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'usagePointGroup', type: 'auto', defaultValue: null},
        {name: 'metrologyPurpose', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false}
    ],
    proxy: {
        type: 'rest',
        url: '/api/dqk/usagePointKpis'
    }
});