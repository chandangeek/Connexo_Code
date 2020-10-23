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
        'signatureAlgorithm',

        'endEntityName',
        'caName',
        'certProfileName',
        'subjectDnFields',

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
