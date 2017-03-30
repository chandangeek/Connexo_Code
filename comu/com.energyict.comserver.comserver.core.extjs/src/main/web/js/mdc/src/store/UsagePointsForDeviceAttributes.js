/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.UsagePointsForDeviceAttributes',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'}
    ],
    pageSize: 50,

    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        reader: {
            type: 'json',
            root: 'usagePoints'
        },
        extraParams: {
            sort: 'name',
            dir: 'ASC'
        }
    }
});