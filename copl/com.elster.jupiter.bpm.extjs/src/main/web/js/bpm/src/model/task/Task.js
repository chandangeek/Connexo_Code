Ext.define('Bpm.model.task.Task', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'processName',
            type: 'string'
        },
        {
            name: 'developmentId',
            type: 'string'
        },
        {
            name: 'dueDate',
            type: 'number',
            convert: function (value, record) {
                return value;
             //   return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : Uni.I18n.translate('bpm.task.noDate', 'BPM', '-');
            }
        },
        {
            name: 'createdOn',
            type: 'number',
            convert: function (value, record) {
                return value;
             //   return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : Uni.I18n.translate('bpm.task.noDate', 'BPM', '-');
            }
        },
        {
            name: 'priority',
            type: 'string'
        },
        {
            name: 'status',
            type: 'string'
        },
        {
            name: 'actualOwner',
            type: 'string'
        },
        {
            name: 'processInstancesId',
            type: 'string'
        }
    ]
});