/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.store.DeviceTypes', {
    extend: 'Ext.data.Store',
    fields: [
        'id', 'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddq/fields/deviceTypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});
