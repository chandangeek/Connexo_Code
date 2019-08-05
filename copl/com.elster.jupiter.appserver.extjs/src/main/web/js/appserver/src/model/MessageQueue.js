/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.MessageQueue', {
    extend: 'Uni.model.Version',
    idProperty: 'name',
    requires: [
        'Apr.model.Subscriber',
        'Apr.model.TaskQueue'
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
            name: 'isDefault',
            mapping: function (data) {
                return data.isDefault;
            }
        },
        {
            name: 'queueTypeName',
            mapping: function (data) {
                return data.queueTypeName;
            }
        },
        {
            name: 'subscriberSpecInfos'
        },
        {
            name: 'tasks'
        },
        {
            name: 'serviceCallTypes'
        }
    ],
    hasMany: {
        model: 'Apr.model.Subscriber',
        name: 'subscriberSpecInfos'
    },
    associations: [
        {
            type: 'hasMany',
            model: 'Apr.model.TaskQueue',
            associationKey: 'tasks',
            name: 'tasks'
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