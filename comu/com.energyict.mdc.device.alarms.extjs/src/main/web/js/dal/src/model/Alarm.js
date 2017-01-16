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
                return data.alarmId + ': ' + data.title;
            }
        },
        {name: 'id', type: 'int'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'status', type: 'auto'},
        {name: 'statusName', type: 'auto', mapping: 'status.name'},
        {name: 'clearedStatus', type: 'auto'},
        {
            name: 'statusDetail',
            convert: function (value, rec) {
                if (rec.get('clearedStatus'))
                    return Ext.String.format(Uni.I18n.translate('device.alarms.statusDetail', 'DAL', '{0} has been cleared on {1}'),
                        rec.get('alarmId'), Uni.DateTime.formatDateTimeShort(new Date(rec.get('dueDate'))));
                return null;
            }
        },
        {
            name: 'cleared',
            convert: function (value, rec) {
                return rec.get('clearedStatus') ? Uni.I18n.translate('device.alarms.cleared.yes', 'DAL', 'Yes') : Uni.I18n.translate('device.alarms.cleared.no', 'DAL', 'No');
            }
        },
        {name: 'userAssignee', type: 'auto'},
        {
            name: 'user',
            persist: false,
            mapping: 'userAssignee.name',
            convert: function (value, rec) {
                return value ? value : Uni.I18n.translate('device.alarms.user.unassigned', 'DAL', 'Unassigned');
            }
        },
        {
            name: 'userId',
            persist: false,
            convert: function (value, record) {
                var userId = record.get('userAssignee').id;
                return userId ? userId : -1;
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
            name: 'usagePointMRID',
            mapping: 'device.usagePoint.name'
        },
        {name: 'deviceMRID', type: 'auto'},
        {
            name: 'location',
            persist: false,
            mapping: 'device.location'
        },

        {name: 'logbook', type: 'auto'},
        {name: 'version', type: 'int'},
        {name: 'device', type: 'auto'},
        {name: 'alarmId', type: 'string'},
        {name: 'status_name', persist: false, mapping: 'status.name'},
        {name: 'deviceName', persist: false, mapping: 'device.name'},
        {name: 'usage_point', persist: false, mapping: 'device.usagePoint.info'},
        {name: 'relatedEvents'}
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Isu.model.IssueComment',
            associationKey: 'comments',
            name: 'comments'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dal/alarms',
        reader: {
            type: 'json'
        }
    }
});