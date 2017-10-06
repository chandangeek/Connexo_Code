/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.model.RegisteredDevicesKPI', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'integer', useNull: true},
        {name: 'deviceGroup', type: 'auto', defaultValue: null},
        {name: 'frequency', type: 'auto', defaultValue: null},
        {name: 'target', type: 'integer', useNull: true, defaultValue: 95},
        {name: 'latestCalculationDate', dateFormat: 'time', type: 'date', persist: false}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/registereddevkpis'
    }
});
