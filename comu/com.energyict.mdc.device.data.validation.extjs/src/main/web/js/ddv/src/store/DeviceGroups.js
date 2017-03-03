/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by david on 6/10/2016.
 */
Ext.define('Ddv.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'}
    ],
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddq/fields/kpiDeviceGroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceGroups'
        }
    }
});