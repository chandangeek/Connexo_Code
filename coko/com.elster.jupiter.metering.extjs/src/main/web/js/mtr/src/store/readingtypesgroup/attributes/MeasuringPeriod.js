/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.store.readingtypesgroup.attributes.MeasuringPeriod', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/basiccodes/measurementPeriod',
        reader: {
            type: 'json',
            root: 'measurementPeriodCodes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});

