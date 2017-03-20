/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.TrustStores', {
    extend: 'Ext.data.Store',
    model: 'Mdc.securityaccessors.model.TrustStore',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'trustStores'
        }
    }

});
