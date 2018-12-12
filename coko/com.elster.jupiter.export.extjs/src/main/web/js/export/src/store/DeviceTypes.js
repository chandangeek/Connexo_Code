/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.DeviceTypes', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.EndDeviceEventTypePart',
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
    }
});