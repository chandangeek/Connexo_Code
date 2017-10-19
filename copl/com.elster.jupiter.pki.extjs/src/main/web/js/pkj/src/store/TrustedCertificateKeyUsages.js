/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustedCertificateKeyUsages', {
    extend: 'Ext.data.Store',
    requires: [
        'Pkj.model.CertificateKeyUsage'
    ],
    model: 'Pkj.model.CertificateKeyUsage',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/pir/truststores/{trustStoreId}/certificates/keyusages',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'keyUsages'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (trustStoreId) {
            this.url = this.urlTpl.replace('{trustStoreId}', trustStoreId);
        }
    }

});