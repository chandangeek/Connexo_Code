Ext.define('Dal.model.Alarm', {
    extend: 'Uni.model.Version',
    requires: [
        //    'Isu.model.IssueReason',
        //    'Isu.model.IssueStatus',
        //     'Isu.model.Device',
        //     'Isu.model.IssueAssignee',
        'Isu.model.IssueComment',
        //    'Isu.model.IssueAction'
    ],
    fields: [
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                if (data.device) {
                    return data.alarmId + ': ' + data.reason.name + ' ' + data.device.name;
                } else if (data.deviceName) {
                    return data.alarmId + ': ' + data.reason.name + ' ' + data.deviceName;
                } else {
                    return data.alarmId + ': ' + data.reason.name;
                }
            }
        },
        {name: 'id', type: 'int'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'modTime', type: 'date', dateFormat: 'time'},
        {name: 'status', type: 'auto'},
        {name: 'statusName', type: 'auto', mapping: 'status.name'},
        {name: 'clearedStatus', type: 'auto'},
        {
            name: 'statusDetail',
            convert: function (value, rec) {
                return '... has been cleared on ...';
            }
        },
        {
            name: 'cleared',
            convert: function (value, rec) {
                return value ? Uni.I18n.translate('device.alarms.cleared.yes', 'DAL', 'Yes') : Uni.I18n.translate('device.alarms.cleared.no', 'DAL', 'No');
            }
        },
        {name: 'assignee', type: 'auto'},
        {
            name: 'user',
            persist: false,
            mapping: 'assignee.name',
            convert: function (value, rec) {
                return value ? value : Uni.I18n.translate('device.alarms.user.unassigned', 'DAL', 'Unassigned');
            }
        },

        {name: 'workGroupAssignee', type: 'auto'},
        {
            name: 'workgroup',
            persist: false,
            mapping: 'workGroupAssignee.name',
            convert: function (value, rec) {
                return value ? value : Uni.I18n.translate('device.alarms.workgroup.unassigned', 'DAL', 'Unassigned');
            }
        },

        {name: 'reason', type: 'auto'},
        {name: 'reasonName', persist: false, mapping: 'reason.name'},
        {
            name: 'usagePointMRID', type: 'auto',
            convert: function (value, rec) {
                return 'UP_010000010001';
            }
        },
        {name: 'deviceMRID', type: 'auto'},
        {name: 'location', type: 'auto'},

        {name: 'logbook', type: 'auto'},


        {name: 'version', type: 'int'},
        {name: 'device', type: 'auto'},
        {name: 'issueType', type: 'auto'},
        {name: 'alarmId', type: 'string'},
        {name: 'issueType_name', persist: false, mapping: 'issueType.name'},

        {name: 'status_name', persist: false, mapping: 'status.name'},
        {name: 'deviceName', persist: false, mapping: 'device.name'},

        {name: 'assignee_type', persist: false, mapping: 'assignee.type'},
        {name: 'usage_point', persist: false, mapping: 'device.usagePoint.info'},
        {name: 'service_location', persist: false, mapping: 'device.serviceLocation.info'},
        {name: 'service_category', persist: false, mapping: 'device.serviceCategory.info'},
        'deviceName',
        'comTaskId',
        'comTaskSessionId',
        'connectionTaskId',
        'comSessionId'
    ],
    associations: [
        /*     {
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
         },*/
        {
            type: 'hasMany',
            model: 'Isu.model.IssueComment',
            associationKey: 'comments',
            name: 'comments'
        },
        /*     {
         type: 'hasMany',
         model: 'Isu.model.IssueAction',
         associationKey: 'actions',
         name: 'actions'
         }*/
    ],
    proxy: {
        type: 'rest',
        url: '/api/dal/alarms',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});