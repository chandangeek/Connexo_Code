/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        'loglevel', 'message',
        {
            name: 'timestamp',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
