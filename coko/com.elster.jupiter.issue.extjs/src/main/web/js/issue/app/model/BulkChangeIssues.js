Ext.define('Mtr.model.BulkChangeIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation',  type: 'string'},
        {name: 'status',  type: 'string'},
        {name: 'comment',  type: 'string'},
        {name: 'assigneeType',  type: 'string'},
        {name: 'assigneeId',  type: 'string'}
    ],

    hasMany: {model: 'Mtr.model.BulkIssues', name: 'issues'}
});