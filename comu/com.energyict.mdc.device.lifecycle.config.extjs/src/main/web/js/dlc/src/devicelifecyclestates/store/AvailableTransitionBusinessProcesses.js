/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess',
    storeId: 'AvailableTransitionBusinessProcesses',
    remoteFilter: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],

    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/statechangebusinessprocesses',
        reader: {
            type: 'json',
            root: 'stateChangeBusinessProcesses'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});