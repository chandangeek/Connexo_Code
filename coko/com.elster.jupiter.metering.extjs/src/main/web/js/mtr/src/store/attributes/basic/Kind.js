/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.Kind', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/measurementkind/{filter}',
        reader: {
            type: 'json',
            root: 'measurementkindCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
