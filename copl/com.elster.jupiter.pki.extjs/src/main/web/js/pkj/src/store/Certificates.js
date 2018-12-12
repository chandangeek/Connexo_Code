/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.store.Certificates', {
    extend: 'Ext.data.Store',
    model: 'Pkj.model.Certificate',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'certificates'
        }
    }

});
