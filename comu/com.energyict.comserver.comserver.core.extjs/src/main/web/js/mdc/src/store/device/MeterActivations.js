/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.device.MeterActivations', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MeterActivation'
    ],
    model: 'Mdc.model.MeterActivation',
    pageSize: undefined,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/history/meteractivations',
        reader: {
            type: 'json',
            root: 'meterActivations'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
