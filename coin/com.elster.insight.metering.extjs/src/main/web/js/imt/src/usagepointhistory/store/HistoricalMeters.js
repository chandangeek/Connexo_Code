/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.store.HistoricalMeters', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointhistory.model.HistoricalMeter',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/history/meters',
        reader: {
            type: 'json',
            root: 'meters'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});
