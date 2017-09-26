/**
 * Created by H251853 on 9/4/2017.
 */
Ext.define('Mdc.model.IssueAlarm', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'issueId', type: 'auto'},
        {name: 'priorityValue', type: 'auto'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'dueDate', type: 'date', dateFormat: 'time'},
        {name: 'issueType', type: 'auto'},
        {
            name: 'status',
            convert: function (value, record) {
                if (value)
                    return value.name;
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
            name: 'userAssignee',
            convert: function (value, record) {
                if (value)
                    return value.name;
                return '';
            }
        },
        {
            name: 'usagePoint',
            mapping: 'device.usagePoint.info'
        },
        {
            name: 'device',
            convert: function (value, record) {
                if (value)
                    return value.location;
                return '';
            }
        },
        {
            name: 'logBook',
            mapping: 'logBook.name'

        }
    ]
});