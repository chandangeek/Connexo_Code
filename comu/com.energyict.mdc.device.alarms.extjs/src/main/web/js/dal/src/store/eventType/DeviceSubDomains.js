/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.eventType.DeviceSubDomains', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.eventType.EndDeviceEventTypePart',
    storeId: 'deviceSubDomains',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/enddeviceeventtypes/devicesubdomains',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'endDeviceEventTypePartInfos'
        }
    }
});