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
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devicegroups/filtered',
        reader: {
            type: 'json',
            root: 'devicegroups'
        }
    }
});