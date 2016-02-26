Ext.define('Scs.model.ServiceCall', {
    extend: 'Uni.model.Version',
    fields: [
        'number', 'externalReference', 'state', 'type',
        {
            name: 'hasChildren',
            type: 'boolean'
        },
        {
            name: 'version',
            defaultValue: 0
        },
        {
            name: 'creationTime',
            type: 'number'
        },
        {
            name: 'lastModificationTime',
            type: 'number'
        },
        {
            name: 'lastCompletedTime',
            type: 'number'
        },
        {
            name: 'creationTimeDisplay',
            type: 'string',
            convert: function (value, record) {
                var creationTime = record.get('creationTime');
                if (creationTime && (creationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(creationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastModificationTimeDisplay',
            type: 'string',
            convert: function (value, record) {
                var lastModificationTime = record.get('lastModificationTime');
                if (lastModificationTime && (lastModificationTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(lastModificationTime));
                }
                return '-';
            }
        },
        {
            name: 'lastCompletedTimeDisplay',
            type: 'string',
            convert: function (value, record) {
                var lastCompletedTime = record.get('lastModificationTime');
                if (lastCompletedTime && (lastCompletedTime !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(lastCompletedTime));
                }
                return '-';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalls',
        timeout: 120000,
        reader: {
            type: 'json'
        }
    }
});