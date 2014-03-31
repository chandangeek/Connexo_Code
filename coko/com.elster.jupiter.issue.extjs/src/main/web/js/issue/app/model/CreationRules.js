Ext.define('Isu.model.CreationRules', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'text'
        },
        {
            name: 'template',
            type: 'auto'
        },
        {
            name: 'reason',
            type: 'auto'
        },
        {
            name: 'status',
            type: 'text'
        },
        {
            name: 'assignee',
            type: 'auto'
        },
        {
            name: 'duein',
            type: 'auto'
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
            type: 'text'
        },
        {
            name: 'creationdate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'modificationdate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creation/rules',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
