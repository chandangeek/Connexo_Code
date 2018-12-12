/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceConfigurations', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
    model: 'Mdc.model.DeviceConfiguration',
    storeId: 'DeviceConfigurations',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        baseUrl: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        reader: {
            type: 'json',
            root: 'deviceConfigurations'
        },
        setUrl: function (params) {
            this.url = this.baseUrl.replace('{deviceType}', params['deviceType'])
        }
    }
});