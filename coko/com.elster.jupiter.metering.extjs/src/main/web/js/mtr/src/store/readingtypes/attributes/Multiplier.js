/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypes.attributes.Multiplier', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/metricMultiplier',
        reader: {
            type: 'json',
            root: 'metricMultiplierCodes'
        },
        limitParam: false
    }
});
