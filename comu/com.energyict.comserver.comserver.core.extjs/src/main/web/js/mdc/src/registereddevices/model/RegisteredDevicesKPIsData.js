/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.model.RegisteredDevicesKPIsData', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'timestamp', type: 'integer', useNull: true},
        {name: 'target', type: 'integer', useNull: true},
        {name: 'total', type: 'integer', useNull: true},
        {name: 'registered', type: 'integer', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/registereddevkpis/kpidata',

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
