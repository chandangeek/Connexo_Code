Ext.define('Mtr.model.AssignmentRules', {
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
            name: 'priority',
            type: 'int'
        },
        {
            name: 'status',
            type: 'string'
        },
        {
            name: 'assignee',
            type: 'auto'
        },
        {
            name: 'when',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'int'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules',
        reader: {
            type: 'json'
        }
    }
});