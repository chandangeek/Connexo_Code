/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.Parities',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.Parity'
    ],
    model: 'Mdc.model.field.Parity',
    autoLoad: false,
    storeId: 'Parities',

    proxy: {
        type: 'rest',
        url: '/api/mdc/field/parity',
        reader: {
            type: 'json',
            root: 'parities'
        }
    }
});

