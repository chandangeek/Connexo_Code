/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceGroup'
    ],
    model: 'Mdc.model.DeviceGroup',
    storeId: 'DeviceGroups',
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups',
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});
