Ext.define('Mtr.model.BulkChangeIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'comment', type: 'string'},
        {name: 'assignee', type: 'auto'}
    ],

    hasMany: {model: 'Mtr.model.BulkIssues', name: 'issues'}
});