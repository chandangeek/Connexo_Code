/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateSubjects', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateSubject',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/subjects',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'subjects'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});
