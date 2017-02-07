/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.DataCompletion', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.DataCompletion',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/validationSummary',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'outputs'
        }
    }
});