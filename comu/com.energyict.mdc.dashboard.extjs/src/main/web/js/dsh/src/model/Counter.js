/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Counter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'int' },
        { name: 'alias', type: 'string' },
        { name: 'displayName', type: 'string' },
        { name: 'name', type: 'string', mapping: function (data) {
           return Ext.isString(data.name) ? data.name.toLowerCase() : ''}
        },
        { name: 'total', type: 'int' }
    ],

    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});