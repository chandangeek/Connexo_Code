/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.eventType.DeviceEventOrActions', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.eventType.EndDeviceEventTypePart',
    storeId: 'deviceEventOrActions',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/enddeviceeventtypes/deviceeventoractions',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'endDeviceEventTypePartInfos'
        }
    }
});