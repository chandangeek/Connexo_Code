/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.Zones', {
    extend: 'Ext.data.Store',
    autoLoad: true,

    fields: [
       'id',
        'zoneTypeName',
        'name',
        'zoneTypeId',
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/zones',
        reader: {
            type: 'json',
            root: 'zones'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
    }
});