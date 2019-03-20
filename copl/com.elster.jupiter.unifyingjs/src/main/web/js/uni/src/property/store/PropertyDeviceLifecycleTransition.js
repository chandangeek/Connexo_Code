/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.store.PropertyDeviceLifecycleTransition', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {   name: 'values',
            type: 'auto'
        }
    ],
    autoLoad: true,

    proxy: {
        type: 'memory'
    }
});