Ext.define('Isu.model.AutoCreationRules', {
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
            type: 'string'
        },
        {
            name: 'active',
            type: 'string'
        },
        {
            name: 'type',
            type: 'string'
        },
        {
            name: 'reason',
            type: 'string'
        },
        {
            name: 'title',
            type: 'string'
        },
        {
            name: 'rule',
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
        url: '/api/isu/rules/auto',
        reader: {
            type: 'json'
        }
    }
});
