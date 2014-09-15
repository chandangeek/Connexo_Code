Ext.define('Dsh.model.ConnectionResults', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'displayValue', type: 'string' },
        { name: 'alias', type: 'string' },
        { name: 'id', type: 'int' },
        'data'
    ],
    hasMany: [
        {
            model: 'Dsh.model.Result',
            name: 'data'
        }
    ]
});
