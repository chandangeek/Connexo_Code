/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.Phase', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/phases',
        reader: {
            type: 'json',
            root: 'phasesCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
