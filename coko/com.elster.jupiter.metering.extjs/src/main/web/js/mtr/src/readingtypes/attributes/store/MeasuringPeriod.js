/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.attributes.store.MeasuringPeriod',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: ['code', 'displayName'],
    proxy: {
        type: 'rest',
        url: '/api/mtr/readingtypes/codes/measurementPeriod',
        reader: {
            type: 'json',
            root: 'measurementPeriodCodes'
        },
        limitParam: false
    }
});

