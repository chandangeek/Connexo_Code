/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.store.LifeCycleAndState', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointhistory.model.LifeCycleAndState',
    proxy: {
        type: 'rest',
        url: '/api/upl/usagepoint/{usagePointId}/transitions/history',
        reader: {
            type: 'json',
            root: 'history'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});
