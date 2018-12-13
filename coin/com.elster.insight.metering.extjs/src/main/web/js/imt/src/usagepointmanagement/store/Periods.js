/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.Periods', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.Period',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/validationSummaryPeriods',
        reader: {
            type: 'json',
            root: 'relativePeriods'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});