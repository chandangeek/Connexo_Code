Ext.define('Dsh.model.UnassignedIssues', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'total', type: 'int' },
        { name: 'filter', type: 'auto' }
    ]
});