Ext.define('Isu.model.CreationRuleActionPhases', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uuid',
            type: 'string'
        },
        {
            name: 'title',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ],

    idProperty: 'uuid'
});