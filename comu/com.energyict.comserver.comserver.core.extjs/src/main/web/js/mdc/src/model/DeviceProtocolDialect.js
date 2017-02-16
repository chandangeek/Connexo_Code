/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceProtocolDialect', {
    extend: 'Mdc.model.ProtocolDialect',
    fields: [
        {name: 'device', defaultValue: null},
        {name: 'version', defaultValue: undefined},
        {name: 'parent', defaultValue: undefined}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/protocoldialects'
    }
});