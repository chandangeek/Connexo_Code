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
            name: 'deploymentId',
            type: 'string'
        },
        {
            name: 'dueDate',
            type: 'number'
        },
        {
            name: 'dueDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('dueDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('dueDate'))) : Uni.I18n.translate('bpm.task.noDate', 'BPM', '-');
            }
        },
        {
            name: 'createdOn',
            type: 'number'
        },
        {
            name: 'createdOnDisplay',
            type: 'number',
            convert: function (value, record) {
                return record.get('createdOn') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('createdOn'))) : Uni.I18n.translate('bpm.task.noDate', 'BPM', '-');
            }
        },
        {
            name: 'priority',
            type: 'number'
        },
        {
            name: 'priorityDisplay',
            type: 'string',
            convert: function (value, record) {
                var priority = record.get('priority');
                if (priority <= 3) {
                    return Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High');
                }
                else if (priority <= 6) {
                    return Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium');
                }
                else {
                    return Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low');
                }
                return '';
            }
        },
        {
            name: 'status',
            type: 'string'
        },
        {
            name: 'statusDisplay',
            type: 'string',
            convert: function (value, record) {
                switch (value){
                    case 'Created':
                    case 'Ready':
                    case 'Reserved':
                        return Uni.I18n.translate('bpm.filter.openStatus', 'BPM', 'Open');
                    case 'InProgress':
                    case 'Suspended':
                        return Uni.I18n.translate('bpm.filter.inProgressStatus', 'BPM', 'In progress');
                    case 'Completed':
                        return Uni.I18n.translate('bpm.filter.completedStatus', 'BPM', 'Completed');
                    case 'Failed':
                    case 'Error':
                    case 'Exited':
                    case 'Obsolete':
                        return Uni.I18n.translate('bpm.filter.inProgressStatus', 'BPM', 'In progress');
                    default:
                        return value;
                }
                return value;
            }
        },
        {
            name: 'actualOwner',
            type: 'string'
        },
        {
            name: 'processInstancesId',
            type: 'string'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/tasks',
        reader: {
            type: 'json'
        }
    }
});