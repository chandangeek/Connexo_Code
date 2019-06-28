/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.DeviceGroup', {
    extend: 'Uni.model.Version',
    fields: [
        {
            name: 'id',
            type: 'int',
            useNull: true
        },
        {
            name: 'name',
            type: 'string',
            useNull: true
        },
        {
            name: 'dynamic',
            type: 'boolean',
            defaultValue: true
        },
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups',
        reader: {
            type: 'json'
        }
    }
});