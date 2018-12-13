/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.UsagePointLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.UsagePointLifeCycle',
    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle',
        reader: {
            type: 'json',
            root: 'lifeCycles'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }

});