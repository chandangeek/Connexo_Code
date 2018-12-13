/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.TrustedCertificate', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'alias',
        'expirationDate',
        'status',

        'hasPrivateKey',
        'keyEncryptionMethod',
        'hasCSR',
        'hasCertificate',

        'type',
        'issuer',
        'subject',
        'certificateVersion',
        'serialNumber',
        'notBefore',
        'notAfter',
        'signatureAlgorithm'
    ],
    proxy: {
        type: 'rest',
        url: '/api/pir/truststores/{trustStoreId}/certificates',
        reader: {
            type: 'json'
        }
    }
});