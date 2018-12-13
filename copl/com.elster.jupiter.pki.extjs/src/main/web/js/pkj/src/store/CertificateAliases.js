/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateAliases', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateAlias',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/aliases',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'aliases'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});
