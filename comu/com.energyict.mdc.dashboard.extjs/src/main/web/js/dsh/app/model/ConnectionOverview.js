Ext.define('Dsh.model.ConnectionOverview', {
    requires: 'Dsh.model.Connection',
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayName', type: 'string' },
        { name: 'alias', type: 'string' }
    ],
    hasMany: {
        model: 'Dsh.model.Connection',
        name: 'counters'
    }
});