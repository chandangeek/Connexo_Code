/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.store.TimeUnits',{
    extend: 'Ext.data.Store',
    requires: [
        'Imt.model.TimeUnit'
    ],
    model: 'Imt.model.TimeUnit',
    storeId: 'TimeUnits',
    proxy: {
        type: 'rest',
        url: '/api/tmr/field/timeUnit',
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});

