/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Usr.store.Certificates', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Certificate',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/pir/certificates',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'certificates'
        },
        limitParam : undefined,
        pageParam : undefined,
        startParam : undefined
    },
    listeners: {
        load: function() {
            this.filter(function(rec){
                return rec.get('status') && Ext.isString(rec.get('status').id) && rec.get('status').id !== 'Revoked';
            });
        }
    }
});
