/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.AvailableKPIs', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.registereddevices.model.AvailableKPI'
    ],
    model: 'Mdc.registereddevices.model.AvailableKPI',
    storeId: 'RegisteredDevicesKPIAvailableKPIs',
    autoload: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/registereddevkpis',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json'
        }
    }
});