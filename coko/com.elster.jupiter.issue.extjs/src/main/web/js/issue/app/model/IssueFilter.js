Ext.define('Isu.model.IssueFilter', {
    extend: 'Ext.data.Model',
    fields: ['id'],

    requires: [
        'Isu.model.IssueStatus',
        'Isu.model.Assignee'
    ],

    hasMany: {
        model: 'Isu.model.IssueStatus',
        associationKey: 'status',
        name: 'status'
    },

    hasOne: {
        model: 'Isu.model.Assignee',
        associationKey: 'assignee',
        name: 'assignee'
    }
});
