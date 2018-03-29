/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustedCertificateStatuses', {
    extend: 'Ext.data.Store',
    requires: [
        'Pkj.model.CertificateStatus'
    ],
    model: 'Pkj.model.CertificateStatus',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/pir/truststores/{trustStoreId}/certificates/statuses',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'statuses'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (trustStoreId) {
            this.url = this.urlTpl.replace('{trustStoreId}', trustStoreId);
        }
    }

});