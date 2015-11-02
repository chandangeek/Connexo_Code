Ext.define('Dbp.deviceprocesses.model.RunningProcessOpenTask', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'number'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'status',
            type: 'string'
        },
        {
            name: 'statusDisplay',
            type: 'string',
            convert: function (value, record) {
                switch (record.get('status')){
                    case 'Created':
                    case 'Ready':
                    case 'Reserved':
                        return Uni.I18n.translate('bpm.filter.openStatus', 'DBP', 'Open');
                    case 'InProgress':
                    case 'Suspended':
                        return Uni.I18n.translate('bpm.filter.inProgressStatus', 'DBP', 'In progress');
                    case 'Completed':
                        return Uni.I18n.translate('bpm.filter.completedStatus', 'DBP', 'Completed');
                    case 'Failed':
                    case 'Error':
                    case 'Exited':
                    case 'Obsolete':
                        return Uni.I18n.translate('bpm.filter.failedStatus', 'DBP', 'Failed');
                    default:
                        return value;
                }
                return value;
            }
        },
        {
            name: 'actualOwner',
            type: 'string'
        }
    ]
});