Ext.define('Dsh.model.TopMyIssue', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'int' },
        { name: 'title', type: 'string' },
        { name: 'dueDate', type: 'auto' }
    ]
});