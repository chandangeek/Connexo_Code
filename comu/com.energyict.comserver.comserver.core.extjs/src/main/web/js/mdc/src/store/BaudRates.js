/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.BaudRates',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.BaudRate'
    ],
    model: 'Mdc.model.field.BaudRate',
    autoLoad: false,
    storeId: 'BaudRates',

    proxy: {
        type: 'rest',
        url: '/api/mdc/field/baudRate',
        reader: {
            type: 'json',
            root: 'baudRates'
        }
    }
});

