Ext.define('Dsh.model.ConnectionBreakdown', {
    requires: 'Dsh.model.Counter',
    extend: 'Dsh.model.Connection',
    fields: [
        { name: 'displayName', type: 'string' },
        { name: 'id', type: 'int' },
        { name: 'successCount', type: 'int' },
        { name: 'failedCount', type: 'int' },
        { name: 'pendingCount', type: 'int' }
    ]
});