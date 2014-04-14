Ext.define('Isu.model.CreationRuleTemplate', {
    extend: 'Ext.data.Model',
    belongsTo: 'Isu.model.CreationRule',
    fields: [
        {
            name: 'uid',
            type: 'string'
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
            name: 'parameters',
            type: 'auto'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
