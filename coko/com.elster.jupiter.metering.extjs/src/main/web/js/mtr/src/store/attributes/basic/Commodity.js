/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.Commodity', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/commodity',
        reader: {
            type: 'json',
            root: 'commodityCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

