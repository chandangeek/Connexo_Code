/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.ManuallyRuleItem', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'deviceId',
            type: 'long'
        },
        {
            name: 'reasonId',
            type: 'text'
        },
        {
            name: 'priority.urgency',
            persist: false,
            type: 'text',
            defaultValue: 25
        },
        {
             name: 'priority.impact',
             persist: false,
             type: 'text',
             defaultValue: 5
        },
        {
            name: 'priority',
            type: 'text',
            defaultValue: '25:5'
        },
        {
            name: 'dueDate',
            type: 'date',
            dateFormat: 'time',
            defaultValue: 0,
        },
        {
            name: 'comment',
            type: 'text',
            defaultValue: null
        },
        {
            name: 'statusId',
            type: 'text',
            defaultValue: 'status.open'
        },
        {
            name: 'assignToWorkgroupId',
            type: 'int',
            defaultValue: -1
        },
        {
            name: 'assignToUserId',
            type: 'int',
            defaultValue: -1
        },

    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issues/bulkadd',
        reader: {
            type: 'json'
        }
    }
});