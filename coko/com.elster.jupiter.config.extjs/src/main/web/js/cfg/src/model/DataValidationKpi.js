/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.model.DataValidationKpi', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'type', defaultValue: 'deviceDataQualityKpi'},
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false}
    ],
    proxy: {
        type: 'rest',
        url: '/api/dqk/kpis'
    }
});