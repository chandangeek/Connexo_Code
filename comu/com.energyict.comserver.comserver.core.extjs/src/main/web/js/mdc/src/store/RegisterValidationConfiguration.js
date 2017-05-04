/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterValidationConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterValidationConfiguration'
    ],
    model: 'Mdc.model.RegisterValidationConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/registers/{registerId}/validation',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
