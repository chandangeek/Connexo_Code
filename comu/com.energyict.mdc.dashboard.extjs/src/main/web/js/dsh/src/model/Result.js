/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Result', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string' },
        { name: 'displayName', type: 'string' },
        { name: 'count', type: 'int' }
    ]
});