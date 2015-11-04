Ext.define('Dbp.deviceprocesses.model.RunningProcess', {
    extend: 'Ext.data.Model',
    requires: [
        'Dbp.deviceprocesses.model.RunningProcessOpenTask'
    ],
    fields: [
        {
            name: 'processId',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'startDate',
            type: 'number'
        },
        {
            name: 'startDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('startDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('startDate'))) : '-';
            }
        },
        {
            name: 'status',
            type: 'number'
        },
        {
            name: 'statusDisplay',
            type: 'string',
            convert: function (value, record) {
                switch (record.get('status')) {
                    case 0:
                        return Uni.I18n.translate('bpm.status.pending', 'DBP', 'Pending');
                    case 1:
                        return Uni.I18n.translate('bpm.status.active', 'DBP', 'Active');
                    case 2:
                        return Uni.I18n.translate('bpm.status.completed', 'DBP', 'Completed');
                    case 3:
                        return Uni.I18n.translate('bpm.status.aborted', 'DBP', 'Aborted');
                    case 4:
                        return Uni.I18n.translate('bpm.status.suspended', 'DBP', 'Suspended');
                    default :
                        return value;
                }
            }
        },
        {
            name: 'startedBy',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'openTasks'
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Dbp.deviceprocesses.model.RunningProcessOpenTask',
            associationKey: 'openTasks',
            name: 'openTasks'
        }
    ]
});