/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.UnitOfMeasures', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/unit/{filter}',
        reader: {
            type: 'json',
            root: 'unitCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
