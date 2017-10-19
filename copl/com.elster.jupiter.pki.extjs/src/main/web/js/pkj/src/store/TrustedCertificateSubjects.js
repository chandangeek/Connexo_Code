/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustedCertificateSubjects', {
    extend: 'Ext.data.Store',
    requires: [
        'Pkj.model.CertificateSubject'
    ],
    model: 'Pkj.model.CertificateSubject',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/pir/truststores/{trustStoreId}/certificates/subjects',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'subjects'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (trustStoreId) {
            this.url = this.urlTpl.replace('{trustStoreId}', trustStoreId);
        }
    }

});