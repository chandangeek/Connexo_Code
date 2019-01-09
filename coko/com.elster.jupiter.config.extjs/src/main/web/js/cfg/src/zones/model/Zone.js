/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.model.Zone', {
    extend: 'Uni.model.Version',
    fields: [
        { name: 'id', type: 'int', useNull: true},
        { name: 'zoneTypeName', type: 'string'},
        { name: 'name', type: 'string'},
        { name: 'zoneTypeId', type: 'int'},
    ],

    proxy: {
        type: 'rest',
        url: '/api/mtr/zones',
        reader: {
            type: 'json'
        }
    }
});
