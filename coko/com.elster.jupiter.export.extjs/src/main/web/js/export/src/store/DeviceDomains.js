/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.DeviceDomains', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.EndDeviceEventTypePart',
    storeId: 'deviceDomains',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/enddeviceeventtypes/devicedomains',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'endDeviceEventTypePartInfos'
        }
    }
});