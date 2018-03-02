/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.store.CrlDeviceGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MeterGroup'
    ],
    model: 'Mdc.model.MeterGroup',
    storeId: 'CrlGroups',
    proxy: {
        type: 'rest',
        url: '/api/ddr/crls/groups',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'deviceGroups'
        }
    }
});
