/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.DataPurgeLog', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'timestamp', dateFormat: 'time', type: 'date'},
        'logLevel',
        'message'
    ]
});