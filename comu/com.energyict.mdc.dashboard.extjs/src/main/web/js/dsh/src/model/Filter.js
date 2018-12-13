/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.Filter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Dsh.model.DateRange'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'currentStates', type: 'auto' },
        { name: 'latestStates', type: 'auto' },
        { name: 'latestResults', type: 'auto' },
        { name: 'comPortPools', type: 'auto' },
        { name: 'comSchedules', type: 'auto' },
        { name: 'comTasks', type: 'auto' },
        { name: 'connectionTypes', type: 'auto' },
        { name: 'deviceGroup', type: 'auto' },
        { name: 'deviceTypes', type: 'auto' },
        { name: 'device', type: 'auto' }
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'startedBetween',
            instanceName: 'startedBetween',
            associationKey: 'startedBetween',
            getterName: 'getStartedBetween',
            setterName: 'setStartedBetween'
        },
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'finishedBetween',
            instanceName: 'finishedBetween',
            associationKey: 'finishedBetween',
            getterName: 'getFinishedBetween',
            setterName: 'setFinishedBetween'
        }
    ]
});