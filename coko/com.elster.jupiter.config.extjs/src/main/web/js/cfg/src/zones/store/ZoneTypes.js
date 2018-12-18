/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.store.ZoneTypes', {
    extend: 'Ext.data.Store',
    model: 'Cfg.zones.model.ZoneType',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/mtr/zones/types',
        reader: {
            type: 'json',
            root: 'types'
        }
    }
});
