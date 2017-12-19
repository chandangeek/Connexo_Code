/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.Accumulation', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/accumulation',
        reader: {
            type: 'json',
            root: 'accumulationCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
