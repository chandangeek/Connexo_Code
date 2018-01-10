/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.attributes.basic.CriticalPeakPeriod', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/criticalpeakperiod',
        reader: {
            type: 'json',
            root: 'criticalpeakperiodCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
