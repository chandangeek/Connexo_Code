/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.BulkDeviceConfigurations', {
    extend: 'Mdc.store.DeviceConfigurations',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
    proxy: {
        type: 'rest',
        baseUrl: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        reader: {
            type: 'json',
            root: 'deviceConfigurations'
        },

        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,

        extraParams: {
            filter: '[{"property":"active","value":true}]'
        },

        setUrl: function (params) {
            this.url = this.baseUrl.replace('{deviceType}', params['deviceType'])
        }
    }
});