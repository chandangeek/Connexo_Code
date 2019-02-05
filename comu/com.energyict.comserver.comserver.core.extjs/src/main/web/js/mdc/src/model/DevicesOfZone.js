/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DevicesOfZone', {
    extend: 'Ext.data.Model',
    //autoLoad: false,
    //model: 'Mdc.model.Device',

    fields: [

        {name: 'name', type: 'string'},

    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/zones/byZoneId',
        reader: {
            type: 'json',
            root: 'zones'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
    }
});