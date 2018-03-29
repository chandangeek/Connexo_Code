/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.CertificateTimeouts', {
    extend: 'Ext.data.Store',
    fields: ['label', 'timeout'],
    data: [
        {
            label: '30 sec',
            timeout: 30000
        },
        {
            label: '1 min',
            timeout: 60000
        },
        {
            label: '2 min',
            timeout: 120000
        },
        {
            label: '5 min',
            timeout: 300000
        }
    ]
});
