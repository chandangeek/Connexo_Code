/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.store.FirmwareStatuses', {
    extend: 'Ext.data.Store',
    requires: [
        'Fwc.model.FirmwareStatus'
    ],
    model: 'Fwc.model.FirmwareStatus',
    storeId: 'FirmwareStatuses',
    autoLoad: false,
    remoteFilter: false,
    proxy: {
        type: 'rest',
        url: '/api/fwc/field/firmwareStatuses',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'firmwareStatuses'
        }
    }
});
