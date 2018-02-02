/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.AllUsagePointTypes', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.UsagePointType',
    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/allusagepointtype',
        reader: {
            type: 'json',
            root: 'usagePointTypes'
        }
    }
});