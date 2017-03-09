/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.Issue', {
    extend: 'Uni.model.Version',
    requires: [
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus',
        'Isu.model.Device',
        'Isu.model.IssueAssignee',
        'Isu.model.IssueWorkgroupAssignee',
        'Isu.model.IssueComment',
        'Isu.model.IssueAction'
    ],
    fields: [
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.issueId + ': ' + data.title;
            }
        },
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'version', type: 'int'},
        {name: 'status', type: 'auto'},
        {name: 'issueType', type: 'auto'},
        {name: 'workGroupAssignee', type: 'auto'},
        {name: 'userAssignee', type: 'auto'},
        {name: 'reason', type: 'auto'},
        {name: 'device', type: 'auto'},
        {name: 'issueId', type: 'string'},
        {name: 'issueType_name', persist: false, mapping: 'issueType.name'},
        {name: 'reason_name', persist: false, mapping: 'reason.name'},
        {name: 'urgency', persist: false, mapping: 'priority.urgency'},
        {name: 'impact', persist: false, mapping: 'priority.impact'},
        {
            name: 'priority',
            persist: false,
            convert: function (value, rec) {
                var impact = value.impact,
                    urgency = value.urgency,
                    priority = (impact + urgency) / 10;
                priority = (priority <= 2) ? Uni.I18n.translate('priority.veryLow', 'ISU', 'Very low ({0})') :
                    (priority <= 4) ? Uni.I18n.translate('priority.low', 'ISU', 'Low ({0})') :
                        (priority <= 6) ? Uni.I18n.translate('priority.medium', 'ISU', 'Medium ({0})') :
                            (priority <= 8) ? Uni.I18n.translate('priority.high', 'ISU', 'High ({0})') :
                                Uni.I18n.translate('priority.veryHigh', 'ISU', 'Very high ({0})');
                return Ext.String.format(priority, impact + urgency);
            }
        },
        {name: 'status_name', persist: false, mapping: 'status.name'},
        {name: 'device_name', persist: false, mapping: 'device.name'},
        {name: 'workgroup_name', persist: false, mapping: 'workGroupAssignee.name'},
        {
            name: 'workgroupId',
            persist: false,
            convert: function (value, record) {
                var workgroupId = record.get('workGroupAssignee').id;
                return workgroupId ? workgroupId : -1;
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
        {name: 'assignee_name', persist: false, mapping: 'userAssignee.name'},
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
            associationKey: 'userAssignee',
            name: 'userAssignee',
            getterName: 'geUserAssignee',
            setterName: 'setUserAssignee'
        },
        {
            type: 'hasOne',
            model: 'Isu.model.IssueWorkgroupAssignee',
            associationKey: 'workGroupAssignee',
            name: 'workGroupAssignee',
            getterName: 'getWorkGroupAssignee',
            setterName: 'setWorkGroupAssignee'
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