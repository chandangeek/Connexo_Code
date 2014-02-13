Ext.define('ViewDataCollectionIssues.model.DataCollectionIssue', {
    extend: 'Ext.data.Model',
    
    fields: [
        { name: 'id', displayValue: 'ID', type: 'auto'},
        { name: 'reason', displayValue: 'Reason', type: 'auto'},
        { name: 'status', displayValue: 'Status', type: 'auto'},
        { name: 'device', displayValue: 'Device', type: 'auto'},
        { name: 'dueDate', displayValue: 'Due date', type: 'auto'},
        { name: 'customer', displayValue: 'Customer', type: 'auto'},
        { name: 'assignee', displayValue: 'Assignee', type: 'auto'},
        { name: 'location', displayValue: 'Location', type: 'auto'},
        { name: 'usagePoint', displayValue: 'Usage point', type: 'auto'},
        { name: 'creationDate', displayValue: 'Creation date', type: 'auto'},
        { name: 'serviceCategory', displayValue: 'Service category', type: 'auto'}
    ]
});