/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.AllDevices', {
    extend: 'Uni.data.store.Filterable',

    fields: [
        'id',
        'name'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }
});
