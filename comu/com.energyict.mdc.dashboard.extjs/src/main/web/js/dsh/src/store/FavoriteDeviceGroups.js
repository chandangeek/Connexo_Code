/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.FavoriteDeviceGroups', {
    extend: 'Ext.data.Store',
    storeId: 'FavoriteDeviceGroups',
    requires: ['Dsh.model.DeviceGroup'],
    model: 'Dsh.model.DeviceGroup',
    proxy: {
        type: 'ajax',
        url: '../../api/dsr/favoritedevicegroups',
        reader: {
            type: 'json',
            root: 'favoriteDeviceGroups'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});