/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Dsh.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'}
    ],
    idProperty: 'name',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }
});