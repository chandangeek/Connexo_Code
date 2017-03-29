/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateTypes', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.CertificateType',

    autoLoad: false,
    data : [
        {
            id: 1,
            name: 'Type 1'
        },
        {
            id: 2,
            name: 'Type 2'
        }
    ]

    //proxy: {
    //    type: 'rest',
    //    urlTpl: '/api/pir/keyencryptionmethods?cryptoType=[ClientCertificate]',
    //    reader: {
    //        type: 'json'
    //    },
    //    pageParam: false,
    //    startParam: false,
    //    limitParam: false
    //}
});
