/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableDataLoggerSlaves', {
    extend: 'Uni.data.store.Filterable',

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'AvailableDataLoggerSlaves',
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/unlinkeddataloggerslaves',
        reader: {
            type: 'json',
            root: 'devices'
        },
        pageParam: null,
        startParam: null,
        limitParam: null
    }
});
