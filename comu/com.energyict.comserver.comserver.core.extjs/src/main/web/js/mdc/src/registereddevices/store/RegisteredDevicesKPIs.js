/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.RegisteredDevicesKPIs', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.registereddevices.model.RegisteredDevicesKPI'
    ],
    model: 'Mdc.registereddevices.model.RegisteredDevicesKPI',
    storeId: 'RegisteredDevicesKPIs',
    proxy: {
        type: 'rest',
        url: '/api/ddr/registereddevkpis',
        reader: {
            type: 'json',
            root: 'kpis'
        }
    }
});

