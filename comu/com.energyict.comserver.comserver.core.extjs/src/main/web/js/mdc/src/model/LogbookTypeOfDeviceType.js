/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LogbookTypeOfDeviceType', {
    extend: 'Uni.model.ParentVersion',

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'obisCode',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/logbooktypes',
        reader: {
            type: 'json',
            root: 'logbookType'
        }
    }
});

