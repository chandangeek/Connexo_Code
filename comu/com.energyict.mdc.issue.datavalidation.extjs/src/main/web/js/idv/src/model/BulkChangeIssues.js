Ext.define('Idv.model.BulkChangeIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'comment', type: 'string'},
        {name: 'assignee', type: 'auto'}
    ],

    hasMany: {model: 'Idv.model.BulkIssues', name: 'issues'}
});