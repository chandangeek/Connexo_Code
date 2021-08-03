/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceCommunicationTask', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'comTask'},
        {name: 'connectionMethod', type: 'string'},
        {name: 'connectionFunctionInfo', type: 'Mdc.model.ConnectionFunction', useNull: true, defaultValue: null},
        {name: 'connectionDefinedOnDevice', type: 'boolean'},
        {name: 'connectionStrategy', type: 'string'},
        {name: 'connectionStrategyKey', type: 'string'},
        {name: 'nextCommunication', dateFormat: 'time', type: 'date'},
        {name: 'lastCommunicationStart', dateFormat: 'time', type: 'date'},
        {name: 'urgency', type: 'int'},
        {name: 'isTracing', type: 'boolean'},
        {name: 'securitySettings', type: 'string'},
        'temporalExpression',
        {name: 'scheduleType', type: 'string'},
        {name: 'scheduleTypeKey', type: 'string'},
        {name: 'scheduleName', type: 'string'},
        {name: 'plannedDate', dateFormat: 'time', type: 'date'},
        {name: 'status', type: 'string'},
        {name: 'isOnHold', type: 'boolean'},
        {name: 'ignoreNextExecutionSpecsForInbound', type: 'boolean'},
        {name: 'maxNumberOfTries', type: 'int'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks'
    }
});