/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.DeviceSecurityKeys', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.securityaccessors.model.DeviceSecurityKey'
    ],
    model: 'Mdc.securityaccessors.model.DeviceSecurityKey',
    storeId: 'DeviceSecurityKeys',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/securityaccessors/keys',
        reader: {
            type: 'json',
            root: 'keys'
        }
    }
});
