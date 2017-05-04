/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.store.RegisterData', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.RegisterReading',
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/registers/{registerId}/data',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});