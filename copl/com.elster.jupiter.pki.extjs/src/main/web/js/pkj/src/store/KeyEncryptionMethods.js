/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.KeyEncryptionMethods', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.KeyEncryptionMethod',

    autoLoad: false,

    data: [
        {
            name: 'DataVault',
            displayName: 'Data vault'
        }
    ]

    //proxy: {
    //    type: 'rest',
    //    //url: '/api/pir/keyencryptionmethods',
    //    url: 'http://localhost:3000/keyencryptionmethods',
    //    reader: {
    //        type: 'json'
    //    },
    //    pageParam: false,
    //    startParam: false,
    //    limitParam: false
    //}
});
