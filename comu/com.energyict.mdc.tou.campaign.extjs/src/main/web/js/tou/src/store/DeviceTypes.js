/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.store.DeviceTypes', {
    extend: 'Ext.data.Store',
    model: 'Tou.model.DeviceType',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});