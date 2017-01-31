/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.DataPurgeHistory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'startDate', dateFormat: 'time', type: 'date'},
        'duration',
        'status'
    ],

    proxy: {
        type: 'rest',
        url: '/api/sys/data/history',
        reader: {
            type: 'json',
            root: ''
        }
    }
});