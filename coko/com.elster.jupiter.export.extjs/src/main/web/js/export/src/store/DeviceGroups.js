/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Dxp.model.DeviceGroup'
    ],
    model: 'Dxp.model.DeviceGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/export/fields/metergroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metergroups'
        }
    }
});
