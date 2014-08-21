Ext.define('Dsh.model.BreakdownCounter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'int' },
        { name: 'displayName', type: 'string' },
        { name: 'successCount', type: 'int' },
        { name: 'failedCount', type: 'int' },
        { name: 'pendingCount', type: 'int' }
    ]
});