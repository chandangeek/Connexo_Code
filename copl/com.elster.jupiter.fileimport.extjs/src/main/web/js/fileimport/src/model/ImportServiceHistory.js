Ext.define('Fim.model.ImportServiceHistory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number'},
        {
            name: 'historyId',
            type: 'number',
            convert: function (value, record) {
                return 38766;
            }
        },
        {
            name: 'importServiceId',
            type: 'number',
            convert: function (value, record) {
                return 2004;
            }
        },

        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {
            name: 'importService', type: 'string', convert: function (value, record) {
            return record.get('trigger');
        }
        },
        {
            name: 'startedOnDisplay',
            type: 'string',
            convert: function (value, record) {
                var startedOn = record.get('startedOn');
                if (startedOn && (startedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(startedOn));
                }
                return '-';
            }
        },
        {
            name: 'finishedOnDisplay',
            type: 'string',
            convert: function (value, record) {
                var finishedOn = record.get('finishedOn');
                if (finishedOn && (finishedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(finishedOn));
                }
                return '-';
            }
        },
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {
            name: 'fileName', type: 'string', convert: function (value, record) {
            return record.get('status');
        }
        },
        {
            name: 'summary', type: 'string', convert: function (value, record) {
            return record.get('status');
        }
        },

        {name: 'trigger', type: 'string'}
    ]
});