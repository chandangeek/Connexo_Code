/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DevicesOfDeviceGroup', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DevicesOfDeviceGroup',

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups/{id}/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }
});
