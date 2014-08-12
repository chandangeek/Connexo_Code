Ext.define('Dsh.model.Device', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string' },
        { name: 'title', type: 'string' },
        'type',
        'configuration'
    ],
    hasOne: [
        {
            model: 'Dsh.model.Type',
            name: 'type'
        },
        {
            model: 'Dsh.model.Configuration',
            name: 'configuration'
        }
    ]
});

