/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Usr.store.TrustStores', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.TrustStore',
    autoLoad: false,
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
