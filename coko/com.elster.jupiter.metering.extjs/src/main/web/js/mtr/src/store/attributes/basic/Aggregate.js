/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.Aggregate', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/aggregate',
        reader: {
            type: 'json',
            root: 'aggregateCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
