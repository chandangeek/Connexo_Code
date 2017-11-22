/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.Commodity', {
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
        limitParam: false
    }
});

