/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateStatuses', {
    extend: 'Ext.data.Store',
    requires: [
        'Pkj.model.CertificateStatus'
    ],
    model: 'Pkj.model.CertificateStatus',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/statuses',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'statuses'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});
