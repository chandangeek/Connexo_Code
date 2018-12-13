/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.store.PurposeSummaryData', {
    extend: 'Uni.data.store.Filterable',
    model: 'Imt.purpose.model.PurposeSummaryData',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/data',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});