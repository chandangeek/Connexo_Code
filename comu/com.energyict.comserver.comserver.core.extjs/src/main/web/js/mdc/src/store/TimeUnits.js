/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.TimeUnits',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.field.TimeUnit'
    ],
    model: 'Mdc.model.field.TimeUnit',
    storeId: 'TimeUnits',
    proxy: {
        type: 'rest',
        url: '/api/mdc/field/timeUnit',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'timeUnits'
        }
    }
});

