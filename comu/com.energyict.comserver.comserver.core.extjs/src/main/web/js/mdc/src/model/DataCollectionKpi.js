/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DataCollectionKpi', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'displayRange', type: 'auto', defaultValue: null},
        {name: 'connectionTarget', type: 'integer', useNull: true, defaultValue: null},
        {name: 'communicationTarget', type: 'integer', useNull: true, defaultValue: null},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/kpis'
    }
});