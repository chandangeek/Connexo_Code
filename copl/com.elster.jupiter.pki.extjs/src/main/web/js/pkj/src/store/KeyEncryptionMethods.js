/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.KeyEncryptionMethods', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.KeyEncryptionMethod',

    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/pir/keyencryptionmethods/asymmetric',
        reader: {
            type: 'json',
            root: 'keyEncryptionMethods'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
