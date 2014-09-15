Ext.define('Dsh.model.Counter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'int' },
        { name: 'alias', type: 'string' },
        { name: 'displayName', type: 'string' },
        { name: 'total', type: 'int' }
    ],

    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});