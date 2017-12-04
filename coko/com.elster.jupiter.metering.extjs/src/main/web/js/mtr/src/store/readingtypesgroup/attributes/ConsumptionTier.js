/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.ConsumptionTier', {
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
