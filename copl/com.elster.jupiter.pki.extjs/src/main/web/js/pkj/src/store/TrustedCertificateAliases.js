/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustedCertificateAliases', {
    extend: 'Ext.data.Store',
    requires: [
        'Pkj.model.CertificateAlias'
    ],
    model: 'Pkj.model.CertificateAlias',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/pir/truststores/{trustStoreId}/certificates/aliases',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'aliases'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (trustStoreId) {
            this.url = this.urlTpl.replace('{trustStoreId}', trustStoreId);
        }
    }

});