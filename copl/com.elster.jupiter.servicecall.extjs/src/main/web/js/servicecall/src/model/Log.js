/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.model.Log', {
    extend: 'Ext.data.Model',
    requires: ['Uni.DateTime'],
    fields: [
        'logLevel',
        'message',
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
                    return Uni.DateTime.formatDateTime(new Date(timestamp), Uni.DateTime.SHORT, Uni.DateTime.LONG);
                }
                return '-';
            }
        }
    ]
});
