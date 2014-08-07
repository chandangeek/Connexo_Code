Ext.define('Dsh.model.ConnectionOverview', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayName', type: 'string' },
        { name: 'alias', type: 'string' }
    ],
    hasMany: {
        model: 'Dsh.model.ConnectionCounter',
        name: 'counters'
    }
});