/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.store.Zones', {
    extend: 'Ext.data.Store',
    model: 'Cfg.zones.model.Zone',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/zones',
        reader: {
            type: 'json',
            root: 'zones'
        },
    }

});
