/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.model.ZoneType', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'int'},
        { name: 'name', type: 'string'}
    ],

    proxy: {
    type: 'rest',
        url: '/api/mtr/zones/types',
        reader: {
        type: 'json'
    }
}
});
