Ext.define('Isu.model.IssueFilter', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'status',
            displayValue: 'Status',
            type: 'auto'
        }
    ],

    hasOne: {
        model: 'Isu.model.Assignee',
        name: 'assignee'
    }
});
