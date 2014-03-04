Ext.define('Mtr.model.AssignmentRule', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id'
        },
        {
            name: 'priority'
        },
        {
            name: 'assignTo'
        },
        {
            name: 'status'
        }
    ],
    hasMany: {
        model: 'Mtr.model.AssignmentRuleItem', name: 'when'
    }
});

Ext.define('Mtr.model.AssignmentRuleItem', {
    extend: 'Ext.data.Model',
    fields: [ 'field', 'value', 'op'],
    belongsTo: 'Mtr.model.AssignmentRule'
})