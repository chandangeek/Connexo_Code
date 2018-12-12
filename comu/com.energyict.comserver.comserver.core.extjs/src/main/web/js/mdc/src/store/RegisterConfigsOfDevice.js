/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterConfigsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Register'
    ],
    model: 'Mdc.model.Register',
    storeId: 'RegisterConfigsOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 90000,
        url: '/api/ddr/devices/{deviceId}/registers',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
