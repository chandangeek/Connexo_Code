/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Breakdown', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.BreakdownCounter'
    ],
    fields: [
        { name: 'displayName', type: 'string' },
        { name: 'alias', type: 'string' },
        { name: 'total', type: 'int'},
        { name: 'totalSuccessCount', type: 'int'},
        { name: 'totalPendingCount', type: 'int'},
        { name: 'totalFailedCount', type: 'int'}
    ],
    hasMany: {
        model: 'Dsh.model.BreakdownCounter',
        name: 'counters'
    }
});