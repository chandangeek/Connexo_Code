/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.ConnectionResults', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayValue', type: 'string' },
        { name: 'alias', type: 'string' },
        { name: 'id', type: 'int' },
        'data'
    ],
    hasMany: [
        {
            model: 'Dsh.model.Result',
            name: 'data'
        }
    ]
});
