/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Isu.model.DeviceGroup'
    ],
    model: 'Isu.model.DeviceGroup',
    autoLoad: false,
    storeId: 'IsuDeviceGroups',
    sorters: [{
        property: 'id',
        direction: 'ASC'
    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups',
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});
