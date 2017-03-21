/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.DeviceSecurityKeys', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.securityaccessors.model.DeviceSecurityAccessor'
    ],
    model: 'Mdc.securityaccessors.model.DeviceSecurityAccessor',
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
