Ext.define('Isu.model.Issue', {
    extend: 'Uni.model.Version',
    requires: [
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus',
        'Isu.model.Device',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueComment',
        'Isu.model.IssueAction'
    ],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'modTime', type: 'date', dateFormat: 'time'},
        {name: 'version', type: 'int'},
        {name: 'status', type: 'auto'},
        {name: 'issueType', type: 'auto'},
        {name: 'assignee', type: 'auto'},
        {name: 'reason', type: 'auto'},
        {name: 'device', type: 'auto'},
        {name: 'issueId', type: 'string'},
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                if (data.device) {
                    return data.issueId +': ' + data.reason.name + data.device.name + ' ' + data.device.serialNumber;
                } else if (data.deviceMRID) {
                    return data.issueId +': ' + data.reason.name + ' ' + data.deviceMRID;
                } else {
                    return data.issueId +': ' + data.reason.name;
                }
            }
        },
        {name: 'issueType_name', persist: false, mapping: 'issueType.name'},
        {name: 'reason_name', persist: false, mapping: 'reason.name'},
        {name: 'status_name', persist: false, mapping: 'status.name'},
        {name: 'device_name', persist: false, mapping: 'device.name'},
        {name: 'assignee_name', persist: false, mapping: 'assignee.name'},
        {name: 'assignee_type', persist: false, mapping: 'assignee.type'},
        {name: 'usage_point', persist: false, mapping: 'device.usagePoint.info'},
        {name: 'service_location', persist: false, mapping: 'device.serviceLocation.info'},
        {name: 'service_category', persist: false, mapping: 'device.serviceCategory.info'},
        'deviceMRID',
        'comTaskId',
        'comTaskSessionId',
        'connectionTaskId',
        'comSessionId'
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason',
            getterName: 'getReason',
            setterName: 'setReason'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueStatus',
            associationKey: 'status',
            name: 'status',
            getterName: 'getStatus',
            setterName: 'setStatus'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.Device',
            associationKey: 'device',
            name: 'device',
            getterName: 'getDevice',
            setterName: 'setDevice'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueAssignee',
            associationKey: 'assignee',
            name: 'assignee',
            getterName: 'getAssignee',
            setterName: 'setAssignee'
        },
        {
            type: 'hasMany',
            model: 'Isu.model.IssueComment',
            associationKey: 'comments',
            name: 'comments'
        },
        {
            type: 'hasMany',
            model: 'Isu.model.IssueAction',
            associationKey: 'actions',
            name: 'actions'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/isu/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});