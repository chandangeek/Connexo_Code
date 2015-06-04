Ext.define('Fim.model.ImportServiceHistory', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number'},
        {name: 'occurrenceId', type: 'number'},
        {name: 'importServiceId', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'importServiceName', type: 'string'},
        {name: 'duration', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'fileName', type: 'string'},
        {name: 'summary', type: 'string'},
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
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/fir/importservices/history',
        reader: {
            type: 'json'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{occurrenceId}', params.occurrenceId);
        }

    }
});