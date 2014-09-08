Ext.define('Dsh.model.Counter', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'count', type: 'int' },
        { name: 'alias', type: 'string' },
        { name: 'displayName', type: 'string' }
    ],

    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});