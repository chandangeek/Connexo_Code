/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    model: 'Tou.model.DeviceGroup',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});