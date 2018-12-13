/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.AvailableDeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.registereddevices.model.AvailableDeviceGroup'
    ],
    model: 'Mdc.registereddevices.model.AvailableDeviceGroup',
    storeId: 'RegisteredDevicesKPIAvailableDeviceGroups',
    proxy: {
        type: 'rest',
        url: '../../api/ddr/registereddevkpis/groups',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'deviceGroups'
        }
    }
});