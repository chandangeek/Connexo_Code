/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableDeviceConfigurations',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
    model: 'Mdc.model.DeviceConfiguration',
    storeId: 'AvailableDeviceConfigurations',
    remoteFilter:true,
    filters: [{
        property: 'active',
        value: true
    }],
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceConfigurations'
        }
    }
});
