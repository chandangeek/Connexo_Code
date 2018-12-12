/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.BreakdownCounter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'int' },
        { name: 'displayName', type: 'string' },
        { name: 'successCount', type: 'int' },
        { name: 'failedCount', type: 'int' },
        { name: 'pendingCount', type: 'int' },
        {
            name: 'total',
            type: 'int',
            persist: false,
            convert: function (v, record) {
                return record.get('successCount') + record.get('failedCount') + record.get('pendingCount');
            }
        }
    ]
});