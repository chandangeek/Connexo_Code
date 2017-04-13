/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.DeviceSecurityCertificates', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.securityaccessors.model.DeviceSecurityCertificate'
    ],
    model: 'Mdc.securityaccessors.model.DeviceSecurityCertificate',
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
