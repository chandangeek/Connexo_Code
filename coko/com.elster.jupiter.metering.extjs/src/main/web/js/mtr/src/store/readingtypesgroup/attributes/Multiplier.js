/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.Multiplier', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/metricMultiplier',
        reader: {
            type: 'json',
            root: 'metricMultiplierCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
