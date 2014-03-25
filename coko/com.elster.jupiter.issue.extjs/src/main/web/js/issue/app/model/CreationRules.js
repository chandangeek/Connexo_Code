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
            type: 'text'
        },
        {
            name: 'reason',
            type: 'text'
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
            name: 'version',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/creation',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
