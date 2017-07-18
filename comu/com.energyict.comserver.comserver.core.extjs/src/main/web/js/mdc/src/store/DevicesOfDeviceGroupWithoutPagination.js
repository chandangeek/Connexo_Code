/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DevicesOfDeviceGroupWithoutPagination', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DevicesOfDeviceGroup',

    proxy: {
        type: 'rest',
        urlTpl: '../../api/ddr/devicegroups/{deviceGroupId}/devices',
        reader: {
            type: 'json',
            root: 'devices'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function (deviceGroupId) {
            this.url = this.urlTpl.replace('{deviceGroupId}', deviceGroupId);
        }
    }
});