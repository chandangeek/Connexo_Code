/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.AllMeterRoles', {
    extend: 'Ext.data.Store',
    remoteFilter: false,
    fields: ['displayName', 'key'],
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/meterroles',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterRoles'
        }
    }
});