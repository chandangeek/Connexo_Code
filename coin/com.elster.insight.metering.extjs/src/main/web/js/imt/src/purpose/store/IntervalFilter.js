/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.IntervalFilter', {
    extend: 'Ext.data.Store',
    model: 'Imt.purpose.model.Filter',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/intervals',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});