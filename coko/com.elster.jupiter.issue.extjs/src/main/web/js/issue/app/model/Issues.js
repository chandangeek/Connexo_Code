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
        {name: 'service_category', mapping: 'device.serviceCategory.info'},
        {
            name: 'status_name_f',
            mapping: function (data) {
                var filterIcon;
                if (data.status) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="status" data-filterValue="' + data.status.id + '"></span>';
                    return data.status.name + ' ' + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'reason_name_f',
            mapping: function (data) {
                var filterIcon;
                if (data.reason) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="reason" data-filterValue="' + data.reason.id + '" data-filterSearch="' + data.reason.name + '"></span>';
                    return data.reason.name + ' ' + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'device_f',
            mapping: function (data) {
                var filterIcon;
                if (data.device) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="meter" data-filterValue="' + data.device.id + '" data-filterSearch="' + data.device.serialNumber + '"></span>';
                    return '<a href="#/devices/' + data.device.serialNumber + '">' + data.device.name + ' ' + data.device.serialNumber + '</a>' + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'assignee_name_f',
            mapping: function (data) {
                var filterIcon;
                if (data.assignee) {
                    filterIcon = '<span class="isu-icon-filter isu-apply-filter" data-filterType="assignee" data-filterValue="' + data.assignee.id + ':' + data.assignee.type + '" data-filterSearch="' + data.assignee.name + '"></span>';
                    return data.assignee.name + filterIcon;
                } else {
                    return '';
                }
            }
        },
        {
            name: 'devicelink',
            mapping: function (data) {
                if (data.device) {
                    return '<a href="#/devices/' + data.device.serialNumber + '">' + data.device.name + ' ' + data.device.serialNumber + '</a>';
                } else {
                    return '';
                }
            }
        }
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
        },
        {
            type: 'hasMany',
            model: 'Isu.model.IssueComments',
            associationKey: 'comments',
            name: 'comments'
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