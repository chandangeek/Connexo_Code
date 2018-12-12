/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.model.Alarm', {
    extend: 'Uni.model.Version',
    requires: [
        'Isu.model.IssueComment'
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
            name: 'statusDetailCleared',
            convert: function (value, rec) {
                if (rec.get('clearedStatus').statusValue)
                    return Ext.String.format(Uni.I18n.translate('device.alarms.statusDetailCleared', 'DAL', 'has been cleared on {0}', Uni.DateTime.formatDateTimeShort(new Date(rec.get('clearedStatus').statusChangeDateTime))));
                return '';
            }
        },
        {name: 'snoozedDateTime', type: 'auto'},
        {
            name: 'statusDetailSnoozed',
            convert: function (value, rec) {
                if (rec.get('status').id == 'status.snoozed')
                    return Ext.String.format(Uni.I18n.translate('device.alarms.snoozeReasonDetail', 'DAL', 'has been snoozed until {0}', Uni.DateTime.formatDateTimeShort(new Date(rec.getData().snoozedDateTime))));
                return '';
            }
        },
        {
            name: 'cleared',
            convert: function (value, rec) {
                return rec.get('clearedStatus').statusValue ? Uni.I18n.translate('device.alarms.cleared.yes', 'DAL', 'Yes') : Uni.I18n.translate('device.alarms.cleared.no', 'DAL', 'No');
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
            mapping: 'device.usagePoint.info'
        },
        {name: 'urgency', persist: false, mapping: 'priority.urgency'},
        {name: 'impact', persist: false, mapping: 'priority.impact'},
        {
            name: 'priority',
            persist: false,
            convert: function (value, rec) {
                var impact = value.impact,
                    urgency = value.urgency,
                    priority = (impact + urgency) / 10;
                priority = (priority <= 2) ? Uni.I18n.translate('priority.veryLow', 'DAL', 'Very low ({0})') :
                    (priority <= 4) ? Uni.I18n.translate('priority.low', 'DAL', 'Low ({0})') :
                        (priority <= 6) ? Uni.I18n.translate('priority.medium', 'DAL', 'Medium ({0})') :
                            (priority <= 8) ? Uni.I18n.translate('priority.high', 'DAL', 'High ({0})') :
                                Uni.I18n.translate('priority.veryHigh', 'DAL', 'Very high ({0})');
                return Ext.String.format(priority, impact + urgency);
            }
        },
        {name: 'deviceMRID', type: 'auto'},
        {
            name: 'location',
            persist: false,
            mapping: 'device.location'
        },

        {name: 'logBook', type: 'auto'},
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