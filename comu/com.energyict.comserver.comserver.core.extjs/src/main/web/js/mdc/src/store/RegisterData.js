/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterData', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterData',
        'Mdc.store.RegisterDataDurations'
    ],

    model: 'Mdc.model.RegisterData',
    storeId: 'RegisterData',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/registers/{registerId}/data',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});