/*
 *
 *  * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 *
 */
Ext.define('Fwc.devicefirmware.model.DeviceFirmwareHistoryModel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'firmwareVersion', type: 'string'},
        {name: 'imageIdentifier', type: 'string'},
        {name: 'triggeredBy', type: 'string'},
        {name: 'plannedDate'},
        {name: 'uploadedDate'},
        {name: 'activationDate'},
        {name: 'result'},
        'firmwareTaskId'
    ]
});

