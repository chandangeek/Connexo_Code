/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustStores', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.TrustStore',
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
