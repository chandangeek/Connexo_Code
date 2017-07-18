/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.Domains',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Domain'
    ],
    model: 'Mdc.model.Domain',
    storeId: 'Domains',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/field/enddevicedomains',
        reader: {
            type: 'json',
            root: 'domains'
        }
    }
});
