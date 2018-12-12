/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.TrustStores', {
    extend: 'Ext.data.Store',
    model: 'Mdc.securityaccessors.model.TrustStore',
    //I took these privileges from the REST resource -> we were getting a 403 otherwise when the user didn't have these rights
    autoLoad: Mdc.privileges.DeviceType.canAdministrate(),
    proxy: {
        type: 'rest',
        url: '/api/dtc/truststores',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'trustStores'
        }
    }

});
