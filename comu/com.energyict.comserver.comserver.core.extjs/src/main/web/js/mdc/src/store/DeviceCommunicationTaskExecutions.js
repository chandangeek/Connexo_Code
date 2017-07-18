/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceCommunicationTaskExecutions', {
    extend: 'Ext.data.Store',
    storeId: 'DeviceCommunicationTaskExecutions',
    requires: ['Mdc.model.DeviceCommunicationTaskExecution'],
    model: 'Mdc.model.DeviceCommunicationTaskExecution',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods/{connectionId}/comsessions/{sessionId}/comtaskexecutionsessions',
        reader: {
            type: 'json',
            root: 'comTaskExecutionSessions'
        }
    }
});