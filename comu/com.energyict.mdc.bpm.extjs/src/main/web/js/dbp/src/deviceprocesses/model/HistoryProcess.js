Ext.define('Dbp.deviceprocesses.model.HistoryProcess', {
    extend: 'Ext.data.Model',
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
            name: 'endDate',
            type: 'number'
        },
        {
            name: 'endDateDisplay',
            type: 'string',
            convert: function (value, record) {
                return record.get('endDate') ? Uni.DateTime.formatDateTimeShort(new Date(record.get('endDate'))) : '-';
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
                        return Uni.I18n.translate('dbp.status.pending', 'DBP', 'Pending');
                    case 1:
                        return Uni.I18n.translate('dbp.status.active', 'DBP', 'Active');
                    case 2:
                        return Uni.I18n.translate('dbp.status.completed', 'DBP', 'Completed');
                    case 3:
                        return Uni.I18n.translate('dbp.status.aborted', 'DBP', 'Aborted');
                    case 4:
                        return Uni.I18n.translate('dbp.status.suspended', 'DBP', 'Suspended');
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
            name: 'duration',
            type: 'number'
        },
        {
            name: 'version',
            type: 'string'
        }
    ]
});