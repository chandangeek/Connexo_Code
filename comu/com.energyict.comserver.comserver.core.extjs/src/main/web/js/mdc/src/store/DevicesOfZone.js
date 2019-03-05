/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DevicesOfZone', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.Device',

    fields: [
        {name: 'name', type: 'string'},
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/zones/byZoneId',
        reader: {
            type: 'json',
            root: 'devices'
        },
    }
});