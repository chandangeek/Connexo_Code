Ext.define('Dsh.model.BreakdownCounter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayName', type: 'string' },
        { name: 'id', type: 'int' },
        { name: 'successCount', type: 'int' },
        { name: 'failedCount', type: 'int' },
        { name: 'pendingCount', type: 'int' }
    ]
});