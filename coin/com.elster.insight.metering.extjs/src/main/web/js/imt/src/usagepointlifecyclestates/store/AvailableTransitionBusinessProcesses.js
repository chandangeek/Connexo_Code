/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.store.AvailableTransitionBusinessProcesses', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.usagepointlifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Imt.usagepointlifecyclestates.model.TransitionBusinessProcess',
    storeId: 'AvailableTransitionBusinessProcesses',

    proxy: {
        type: 'rest',
        url: '/api/upl/lifecycle/processes',
        reader: {
            type: 'json',
            root: 'processes'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});