/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableDeviceTypes',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceType'
    ],
    model: 'Mdc.model.DeviceType',
    storeId: 'AvailableDeviceTypes',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    filters: [
        function(deviceType) {
            return deviceType.get('deviceTypePurpose') !== 'MULTI_ELEMENT_SLAVE';
        }
    ],
    filterOnLoad: true,
    remoteFilter: false,
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});
