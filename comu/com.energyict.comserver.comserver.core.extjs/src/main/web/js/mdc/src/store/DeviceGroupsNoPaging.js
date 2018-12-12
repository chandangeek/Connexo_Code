/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceGroupsNoPaging', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceGroup'
    ],
    model: 'Mdc.model.DeviceGroup',
    storeId: 'DeviceGroupsNoPaging',
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devicegroups',
        reader: {
            type: 'json',
            root: 'devicegroups'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
