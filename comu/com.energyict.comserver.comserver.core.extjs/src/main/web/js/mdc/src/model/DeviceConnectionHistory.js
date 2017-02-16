/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceConnectionHistory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'connectionMethod'},
        {name: 'startedOn', dateFormat: 'time', type: 'date'},
        {name: 'finishedOn', dateFormat: 'time', type: 'date'},
        {name: 'durationInSeconds', type: 'int'},
        {name: 'direction', type: 'string'},
        {name: 'connectionType', type: 'string'},
        'comServer',
        {name: 'comPort', type: 'string'},
        {name: 'status', type: 'string'},
        'result',
        'comTaskCount',
        {name: 'isDefault', type: 'boolean'},
        {name: 'device'},
        {name: 'deviceConfiguration'},
        {name: 'deviceType'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods/{connectionId}/comsessions'
    }
});