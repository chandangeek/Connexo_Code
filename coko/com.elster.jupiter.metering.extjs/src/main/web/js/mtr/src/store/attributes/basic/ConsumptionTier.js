/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.ConsumptionTier', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/consumptiontier',
        reader: {
            type: 'json',
            root: 'consumptiontierCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
