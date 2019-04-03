/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.Issue', {
    extend: 'Uni.model.Version',
    requires: [
        'Isu.model.IssueComment'
    ],
    fields: [
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.issueId + ': ' + data.title;
            }
        },
        {name: 'id', type: 'int'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'status', type: 'auto'},
        {name: 'statusName', type: 'auto', mapping: 'status.name'},
        {name: 'snoozedDateTime', type: 'auto'},
        {
            name: 'statusDetailSnoozed',
            convert: function (value, rec) {
                if (rec.get('status').id == 'status.snoozed')
                    return Ext.String.format(Uni.I18n.translate('device.issues.snoozeReasonDetail', 'ITK', 'has been snoozed until {0}', Uni.DateTime.formatDateTimeShort(new Date(rec.getData().snoozedDateTime))));
                return '';
            }
        },
        {name: 'userAssignee', type: 'auto'},
        {
            name: 'user',
            persist: false,
            mapping: 'userAssignee.name',
            convert: function (value, rec) {
                return value ? value : Uni.I18n.translate('device.issues.user.unassigned', 'ITK', 'Unassigned');
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
                return value ? value : Uni.I18n.translate('device.issues.workgroup.unassigned', 'ITK', 'Unassigned');
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
                priority = (priority <= 2) ? Uni.I18n.translate('priority.veryLow', 'ITK', 'Very low ({0})') :
                    (priority <= 4) ? Uni.I18n.translate('priority.low', 'ITK', 'Low ({0})') :
                        (priority <= 6) ? Uni.I18n.translate('priority.medium', 'ITK', 'Medium ({0})') :
                            (priority <= 8) ? Uni.I18n.translate('priority.high', 'ITK', 'High ({0})') :
                                Uni.I18n.translate('priority.veryHigh', 'ITK', 'Very high ({0})');
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
        {name: 'issueId', type: 'string'},
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
        url: '/api/itk/issues',
        reader: {
            type: 'json'
        }
    }
});