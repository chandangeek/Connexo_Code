/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustedCertificateIssuers', {
    extend: 'Ext.data.Store',
    requires: [
        'Pkj.model.CertificateIssuer'
    ],
    model: 'Pkj.model.CertificateIssuer',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/pir/truststores/{trustStoreId}/certificates/issuers',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'issuers'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (trustStoreId) {
            this.url = this.urlTpl.replace('{trustStoreId}', trustStoreId);
        }
    }

});