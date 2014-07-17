Ext.define('Isu.model.CreationRuleAction', {
    extend: 'Ext.data.Model',
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'phase',
            type: 'auto'
        },
        {
            name: 'parameters',
            type: 'auto'
        }
    ]
});
