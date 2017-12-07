/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateKeyUsages', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateKeyUsage',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/keyusages',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'keyUsages'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});
