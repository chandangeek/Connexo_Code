/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTasksOfDevice', {
    extend: 'Ext.data.Store',
    storeId: 'CommunicationTasksOfDevice',
    model: 'Mdc.model.DeviceCommunicationTask',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks',
        reader: {
            type: 'json',
            root: 'comTasks'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});


