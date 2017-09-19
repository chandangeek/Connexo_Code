/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.RegisteredDevicesKPIsData', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.registereddevices.model.RegisteredDevicesKPIsData'
    ],
    model: 'Mdc.registereddevices.model.RegisteredDevicesKPIsData',
    storeId: 'RegisteredDevicesKPIsData',
    proxy: {
        type: 'rest',
        url: '/api/ddr/registereddevkpis/kpidata',

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

