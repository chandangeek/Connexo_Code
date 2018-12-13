/**
 * Created by H251853 on 9/4/2017.
 */
Ext.define('Mdc.model.IssueAlarm', {
    extend: 'Uni.model.Version',
    fields: [
        'deviceName',
        'comTaskId',
        'comTaskSessionId',
        'connectionTaskId',
        'comSessionId',
        'priority',
        'status',
        'title',
        'device',
        'snoozedDateTime',
        'logBook',
        'location',
        {name: 'id', type: 'int'},
        {name: 'issueId', type: 'auto'},
        {
            name: 'priorityValue',
            persist: false,
            convert: function (value, rec) {
                var impact = rec.get('priority').impact,
                    urgency = rec.get('priority').urgency,
                    priority = (impact + urgency) / 10;
                priority = (priority <= 2) ? Uni.I18n.translate('priority.veryLow', 'MDC', 'Very low ({0})') :
                    (priority <= 4) ? Uni.I18n.translate('priority.low', 'MDC', 'Low ({0})') :
                        (priority <= 6) ? Uni.I18n.translate('priority.medium', 'MDC', 'Medium ({0})') :
                            (priority <= 8) ? Uni.I18n.translate('priority.high', 'MDC', 'High ({0})') :
                                Uni.I18n.translate('priority.veryHigh', 'MDC', 'Very high ({0})');
                return Ext.String.format(priority, impact + urgency);
            }
        },
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'issueType', type: 'auto'},
        {
            name: 'statusName',
            convert: function (value, record) {
                if (record.get('status'))
                    return record.get('status').name;
                return '';
            }
        },
        {
            name: 'issueTypeName',
            convert: function (value, record) {
                return record.get('issueType').name;
            }
        },
        {
            name: 'reason',
            convert: function (value, record) {
                if (value)
                    return value.name;
                return '';
            }
        },
        {
            name: 'workGroupAssignee',
            convert: function (value, record) {
                if (value)
                    return value.name;
                return '';
            }
        },
        {
            name: 'userAssigneeName',
            convert: function (value, record) {
                if (record.get('userAssignee')) {
                    return record.get('userAssignee').name;
                }
                return '';
            }
        },
        {
            name: 'usagePoint',
            mapping: 'device.usagePoint.info'
        },
        {
            name: 'location',
            convert: function (value, record) {
                var device = record.get('device');
                if (device && !Ext.isEmpty(device.location)) {
                    return Ext.String.htmlEncode(device.location).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>');
                    return '-'
                }
            }
        },
        {name: 'userAssignee', type: 'auto'},
        {
            name: 'userId',
            persist: false,
            convert: function (value, record) {
                var userId = record.get('userAssignee').id;
                return userId ? userId : -1;
            }
        }
    ]
});