Ext.define('Bpm.monitorissueprocesses.model.IssueProcess', {
    extend: 'Ext.data.Model',
    requires: [
        'Bpm.monitorprocesses.model.RunningProcessOpenTask'
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
                return record.get('startDate') ? Uni.DateTime.formatDateTimeLong(new Date(record.get('startDate'))) : ' ';
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
                        return Uni.I18n.translate('bpm.status.pending', 'BPM', 'Pending');
                    case 1:
                        return Uni.I18n.translate('bpm.status.active', 'BPM', 'Active');
                    case 2:
                        return Uni.I18n.translate('bpm.status.completed', 'BPM', 'Completed');
                    case 3:
                        return Uni.I18n.translate('bpm.status.aborted', 'BPM', 'Aborted');
                    case 4:
                        return Uni.I18n.translate('bpm.status.suspended', 'BPM', 'Suspended');
                    default :
                        return value;
                }
            }
        },
    {
        name: 'statusIcon',
        type: 'string',
        convert: function (value, record) {
            switch (record.get('status')) {
                case 1:
                    return '<span class="icon-radio-unchecked"/>';
                case 2:
                    return '<span class="icon-circle2"/>';
                case 3:
                    return '<span class="icon-circle2"/>';
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
            model: 'Bpm.monitorprocesses.model.RunningProcessOpenTask',
            associationKey: 'openTasks',
            name: 'openTasks'
        }
    ]
});