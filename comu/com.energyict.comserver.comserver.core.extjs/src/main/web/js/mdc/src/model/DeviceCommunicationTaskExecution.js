/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceCommunicationTaskExecution', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        'comTasks',
        'device',
        'deviceConfiguration',
        'deviceType',
        {name: 'comScheduleName', type: 'string'},
        {name: 'urgency', type: 'int'},
        {name: 'currentState', type: 'string'},
        {name: 'result', type: 'string'},
        'comScheduleFrequency',
        {name: 'startTime', dateFormat: 'time', type: 'date'},
        {name: 'finishTime', dateFormat: 'time', type: 'date'},
        {name: 'alwaysExecuteOnInbound', type: 'boolean'},
        {name: 'durationInSeconds', type: 'int'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods/{connectionId}/comsessions/{sessionId}/comtaskexecutionsessions'
    }
});