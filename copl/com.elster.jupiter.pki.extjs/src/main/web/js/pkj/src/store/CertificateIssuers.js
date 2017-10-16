/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateIssuers', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateIssuer',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/issuers',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'issuers'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});
