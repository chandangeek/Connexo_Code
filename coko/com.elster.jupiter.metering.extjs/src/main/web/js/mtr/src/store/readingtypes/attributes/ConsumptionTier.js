/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypes.attributes.ConsumptionTier', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/consumptiontier',
        reader: {
            type: 'json',
            root: 'consumptiontierCodes'
        },
        limitParam: false
    }
});
