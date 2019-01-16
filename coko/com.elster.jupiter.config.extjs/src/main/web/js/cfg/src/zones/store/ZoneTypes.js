/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.store.ZoneTypes', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    fields: [
        'id',
        'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/zones/types',
        reader: {
            type: 'json',
            root: 'types'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
    }
});
