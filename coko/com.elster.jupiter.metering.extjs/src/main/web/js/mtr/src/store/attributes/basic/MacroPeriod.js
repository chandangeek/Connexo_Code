/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.MacroPeriod', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/macroperiod/{filter}',
        reader: {
            type: 'json',
            root: 'macroperiodCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
