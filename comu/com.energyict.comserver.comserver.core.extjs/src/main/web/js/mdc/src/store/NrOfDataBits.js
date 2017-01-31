/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.NrOfDataBits',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.NrOfDataBits'
    ],
    model: 'Mdc.model.field.NrOfDataBits',
    autoLoad: false,
    storeId: 'NrOfDataBits',

    proxy: {
        type: 'rest',
        url: '/api/mdc/field/nrOfDataBits',
        reader: {
            type: 'json',
            root: 'nrOfDataBits'
        }
    }
});

