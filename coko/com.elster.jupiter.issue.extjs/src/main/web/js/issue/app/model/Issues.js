Ext.define('Isu.model.Issues', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'reason',
            displayValue: 'Reason',
            type: 'auto'
        },
        {
            name: 'status',
            displayValue: 'Status',
            type: 'auto'
        },
        {
            name: 'device',
            displayValue: 'Device',
            type: 'auto'
        },
        {
            name: 'dueDate',
            displayValue: 'Due date',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'customer',
            displayValue: 'Customer',
            type: 'auto'
        },
        {
            name: 'assignee',
            displayValue: 'Assignee',
            type: 'auto'
        },
        {
            name: 'creationDate',
            displayValue: 'Creation date',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            displayValue: 'Version',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issue',
        reader: {
            type: 'json'
        }
    }
});