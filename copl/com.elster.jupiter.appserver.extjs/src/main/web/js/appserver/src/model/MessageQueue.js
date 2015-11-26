Ext.define('Apr.model.MessageQueue', {
    extend: 'Uni.model.Version',
    idProperty: 'name',
    fields: [
        'name', 'type', 'active', 'buffered', 'retryDelayInSeconds',
        {
            name: 'numberOfRetries',
            type: 'int'
        },
        {
            name: 'numberOfMessages',
            type: 'int'
        },
        {
            name: 'numberOFErrors',
            type: 'int'
        },
        {
            name: 'retryDelayInMinutes',
            type: 'int',
         //   persist: false,
            mapping:  function (data) {
                if (data.retryDelayInSeconds) {
                    return data.retryDelayInSeconds / 60;
                }
            }
        }

    ],

    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec',
        timeout: 120000,
        reader: {
            type: 'json'
        }
    }
});