Ext.define('Isu.model.CreationRule', {
    extend: 'Ext.data.Model',
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'parameters',
            type: 'auto'
        },
        {
            name: 'actions',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'modificationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        },
        {
            name: 'template',
            type: 'auto'
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'reason',
            type: 'auto'
        },
        {
            name: 'dueIn',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
