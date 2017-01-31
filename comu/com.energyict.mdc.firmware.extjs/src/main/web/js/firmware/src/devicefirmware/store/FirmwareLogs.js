/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.store.FirmwareLogs', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.devicefirmware.model.FirmwareLog'
    ],
    model: 'Fwc.devicefirmware.model.FirmwareLog',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks/{firmwareComTaskId}/comtaskexecutionsessions/{firmwareComTaskSessionId}/journals',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'comTaskExecutionSessions',
            totalProperty: 'total'
        }
    }
});
