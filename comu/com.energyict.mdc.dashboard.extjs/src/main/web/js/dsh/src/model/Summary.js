Ext.define('Dsh.model.Summary', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.Counter'
    ],
    fields: [
        { name: 'total', type: 'int'}
    ],
    hasMany: {
        model: 'Dsh.model.Counter',
        name: 'counters'
    }
});