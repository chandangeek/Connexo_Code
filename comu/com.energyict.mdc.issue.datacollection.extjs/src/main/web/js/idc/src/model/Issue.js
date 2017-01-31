/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.model.Issue', {
    extend: 'Isu.model.Issue',
    fields: [
        'deviceConfiguration', 'deviceType', 'deviceState', 'deviceName', 'slaveDeviceId',
        'connectionAttemptsNumber', 'connectionTask', 'communicationTask',
        {name: 'firstConnectionAttempt', type: 'date', dateFormat: 'time'},
        {name: 'lastConnectionAttempt', type: 'date', dateFormat: 'time'},
        {name: 'deviceState_name', persist: false, mapping: 'deviceState.name'},
        {name: 'connectionMethod_name', persist: false, mapping: 'connectionTask.connectionMethod.name'},
        {name: 'connectionTask_latestAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'connectionTask.latestAttempt'},
        {name: 'connectionTask_latestStatus_name', persist: false, mapping: 'connectionTask.latestStatus.name'},
        {name: 'connectionTask_latestResult_name', persist: false, mapping: 'connectionTask.latestResult.name'},
        {name: 'connectionTask_lastSuccessfulAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'connectionTask.lastSuccessfulAttempt'},
        {name: 'communicationTask_name', persist: false, mapping: 'communicationTask.name'},
        {name: 'latestConnectionUsed_name', persist: false, mapping: 'communicationTask.latestConnectionUsed.name'},
        {name: 'communicationTask_latestAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'communicationTask.latestAttempt'},
        {name: 'communicationTask_latestStatus_name', persist: false, mapping: 'communicationTask.latestStatus.name'},
        {name: 'communicationTask_latestResult_name', persist: false, mapping: 'communicationTask.latestResult.name'},
        {name: 'communicationTask_lastSuccessfulAttempt', type: 'date', dateFormat: 'time', persist: false, mapping: 'communicationTask.lastSuccessfulAttempt'},
        {name: 'connectionTask_journals', persist: false, mapping: 'connectionTask.journals'},
        {name: 'communicationTask_journals', persist: false, mapping: 'communicationTask.journals'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/idc/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});