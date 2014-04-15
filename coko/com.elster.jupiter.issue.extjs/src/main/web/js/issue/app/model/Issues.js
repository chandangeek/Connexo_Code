Ext.define('Isu.model.Issues', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus',
        'Isu.model.Device',
        'Isu.model.Assignee'
    ],
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
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
            name: 'creationDate',
            displayValue: 'Creation date',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            displayValue: 'Version',
            type: 'auto'
        },
        {
            name: 'title', mapping: function (data) {
                // todo: internationalisation
                return data.reason.name + (data.device ? ' to ' + data.device.name + ' ' + data.device.serialNumber : '');
            }
        },
        {name: 'reason_name', mapping: 'reason.name'},
        {name: 'status_name', mapping: 'status.name'},
        {name: 'device_name', mapping: 'device.name'},
        {name: 'assignee_name', mapping: 'assignee.name'},
        {name: 'assignee_type', mapping: 'assignee.type'},
        {name: 'usage_point', mapping: 'device.usagePoint.info'},
        {name: 'service_location', mapping: 'device.serviceLocation.info'},
        {name: 'service_category', mapping: 'device.serviceCategory.info'}
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueStatus',
            associationKey: 'status',
            name: 'status'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Device',
            associationKey: 'device',
            name: 'device'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Assignee',
            associationKey: 'assignee',
            name: 'assignee'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/isu/issue',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});