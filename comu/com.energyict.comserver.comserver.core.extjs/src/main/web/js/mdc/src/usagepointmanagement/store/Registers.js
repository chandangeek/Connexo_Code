/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.store.Registers', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.Register',
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/registers',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'registers'
        }
    }
});