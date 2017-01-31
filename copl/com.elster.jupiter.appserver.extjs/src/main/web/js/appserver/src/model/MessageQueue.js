/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.MessageQueue', {
    extend: 'Uni.model.Version',
    idProperty: 'name',
    requires: [
        'Apr.model.Subscriber'
    ],
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
            mapping:  function (data) {
                if (data.retryDelayInSeconds) {
                    return data.retryDelayInSeconds / 60;
                }
            }
        },
        {
            name: 'subscriberSpecInfos'
        }
    ],
    hasMany: {
        model: 'Apr.model.Subscriber',
        name: 'subscriberSpecInfos'
    },

    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec',
        timeout: 120000,
        reader: {
            type: 'json'
        }
    }
});