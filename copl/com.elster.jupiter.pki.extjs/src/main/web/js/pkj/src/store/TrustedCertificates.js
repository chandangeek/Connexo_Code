/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.TrustedCertificates', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.TrustedCertificate',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores/{trustStoreId}/certificates',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'certificates'
        }
    }

});
