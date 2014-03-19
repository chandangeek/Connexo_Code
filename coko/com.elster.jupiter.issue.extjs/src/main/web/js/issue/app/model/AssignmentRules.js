Ext.define('Isu.model.AssignmentRules', {
    extend: 'Ext.data.Model',
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
            name: 'description',
            type: 'string'
        },
        {
            name: 'assignee',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/assign',
        reader: {
            type: 'json'
        }
    }
});