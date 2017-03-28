/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.Certificate', {
    extend: 'Uni.model.Version',
    fields: [
        'id',
        'alias',
        'expirationDate',
        'status',
        'version',

        'hasPrivateKey',
        'keyEncryptionMethod',

        'hasCSR',
        'hasCertificate',
        'type',
        'issuer',
        'subject',
        'serialNumber',
        'NotBefore',
        'NotAfter',
        'signatureAlgorithm',

        {
            name: 'file',
            useNull: true
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates',
        reader: {
            type: 'json'
        }
    }
});