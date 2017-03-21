/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.DeviceSecurityCertificates', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.securityaccessors.model.DeviceSecurityAccessor'
    ],
    model: 'Mdc.securityaccessors.model.DeviceSecurityAccessor',
    storeId: 'DeviceSecurityCertificates',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/securityaccessors/certificates',
        reader: {
            type: 'json',
            root: 'certificates'
        }
    }
});
