Ext.define('Scs.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        'logLevel', 'message',
        {
            name: 'timestamp',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'timestampDisplay',
            type: 'string',
            convert: function (value, record) {
                var timestamp = record.get('timestamp');
                if (timestamp && (timestamp !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(timestamp));
                }
                return '-';
            }
        },
    ]
});
