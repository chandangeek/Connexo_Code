Ext.define('Isu.model.IssueFilter', {
    extend: 'Isu.component.filter.model.Filter',

    requires: [
        'Isu.model.IssueStatus',
        'Isu.model.Assignee'
    ],

    hasMany: {
        model: 'Isu.model.IssueStatus',
        associationKey: 'status',
        name: 'status'
    },

    hasOne: [
        {
            model: 'Isu.model.Assignee',
            associationKey: 'assignee',
            name: 'assignee'
        },
        {
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason'
        }
    ]
});
