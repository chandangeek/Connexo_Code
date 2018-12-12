/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.model.Logs', {
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
