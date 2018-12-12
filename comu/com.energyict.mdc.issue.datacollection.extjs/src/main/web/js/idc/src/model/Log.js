/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timestamp', type: 'date', dateFormat: 'time'},
        'details', 'logLevel'
    ]
});

