/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.RegisterFilter', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'name', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/registers',
        reader: {
            type: 'json',
            root: 'registers'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});