/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.Certificate', {
    extend: 'Ext.data.Model',
    fields: [
        'alias',
        'expirationDate',
        'type',
        'issuer',
        'subject',
        'status'
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates',
        reader: {
            type: 'json'
        }
    }
});