/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.TrustedCertificate', {
    extend: 'Ext.data.Model',
    fields: [
        'alias',
        'expirationDate'
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores/{trustStoreId}/certificates',
        reader: {
            type: 'json'
        }
    }
});