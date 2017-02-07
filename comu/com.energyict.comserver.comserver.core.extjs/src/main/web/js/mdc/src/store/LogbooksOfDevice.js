/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LogbooksOfDevice', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.LogbookOfDevice',
    storeId: 'LogbooksOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/logbooks',
        reader: {
            type: 'json',
            root: 'data'
        },
        timeout: 300000
    }
});