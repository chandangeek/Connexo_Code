/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.store.DeviceTypes', {
    extend: 'Ext.data.Store',
    storeId: 'deviceTypes',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/enddeviceeventtypes/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'endDeviceEventTypePartInfos'
        }
    },

    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {   name: 'mnemonic',
            type: 'string'
        },
        {   name: 'value',
            type: 'int'
        },
        {   name: 'displayName',
            type: 'string'
        }
    ]
});