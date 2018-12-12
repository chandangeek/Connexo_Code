/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.store.DeviceGroups', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.DeviceGroup'],
    model: 'Est.estimationtasks.model.DeviceGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/est/metergroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});
