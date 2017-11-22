/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.Kind', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/measurementkind',
        reader: {
            type: 'json',
            root: 'measurementkindCodes'
        },
        limitParam: false
    }
});
