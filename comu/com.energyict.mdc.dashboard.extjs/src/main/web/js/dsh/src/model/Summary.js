/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Summary', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.Counter'
    ],
    fields: [
        { name: 'total', type: 'int' },
        { name: 'target', type: 'int' },
        { name: 'alias', type: 'string' }
    ],
    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});