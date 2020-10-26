/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.model.Csr', {
    extend: 'Uni.model.Version',
    fields: [
        'alias',
        'keyTypeId',
        'keyEncryptionMethod',
        'CN',
        'OU',
        'O',
        'L',
        'ST',
        'C',
        'caName',
        'endEntityName',
        'certificateProfileName',
        'subjectDnFields'
    ],

    proxy: {
        type: 'rest',
        url: '/api/pir/certificates/csr',
        reader: {
            type: 'json'
        }
    }

});