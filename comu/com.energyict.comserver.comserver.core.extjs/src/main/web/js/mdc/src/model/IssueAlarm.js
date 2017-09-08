/**
 * Created by H251853 on 9/4/2017.
 */
Ext.define('Mdc.model.IssueAlarm', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'issueId', type: 'auto'},
        {name: 'alarmId', type: 'auto'},
        {name: 'issueType', type: 'auto'},
        {name: 'alarmType', type: 'auto'},
        {name: 'reason', type: 'auto'},
        {name: 'priorityValue', type: 'auto'},
        {name: 'creationDate', type: 'date', dateFormat: 'time'},
        {name: 'status', type: 'auto'},
        {name: 'workgroup', type: 'auto'},
        {name: 'user', type: 'auto'},
        {
            name: 'itemId',
            convert: function (value, record) {
                if (record.get('issueId'))
                    return record.get('issueId');
                return record.get('alarmId');
            }
        },
        {
            name: 'itemType',
            convert: function (value, record) {
                if (record.get('issueType'))
                    return record.get('issueType').name;
                return record.get('alarmType').name;
            }
        }
    ]
});