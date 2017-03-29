/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateTypes', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateType',

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/pir/keytypes/forCsrCreation',
        reader: {
            type: 'json',
            root: 'keyTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
