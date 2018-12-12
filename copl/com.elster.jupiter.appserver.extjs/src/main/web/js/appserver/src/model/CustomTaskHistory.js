/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.CustomTaskHistory', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'task', persist: false},
        {name: 'id', type: 'number'},
        {name: 'startedOn', type: 'number'},
        {name: 'finishedOn', type: 'number'},
        {name: 'duration', type: 'number'},
        {name: 'summary', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'reason', type: 'string'},
        {name: 'statusDate', type: 'number'},
        {name: 'statusPrefix', type: 'string'},
        {
            name: 'name',
            persist: false,
            mapping: function (data) {
                return data.task.name;
            }
        },
        {
            name: 'logLevel',
            persist: false,
            mapping: function (data) {
                return data.task.logLevel;
            }
        },
        {
            name: 'statusOnDate',
            persist: false,
            mapping: function (data) {
                if (data.statusDate && (data.statusDate !== 0)) {
                    return data.statusPrefix + ' ' + Uni.DateTime.formatDateTimeLong(new Date(data.statusDate));
                }
                return data.statusPrefix;
            }
        },
        {
            name: 'startedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.startedOn && (data.startedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.startedOn));
                }
                return '-';
            }
        },
        {
            name: 'finishedOn_formatted',
            persist: false,
            mapping: function (data) {
                if (data.finishedOn && (data.finishedOn !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(data.finishedOn));
                }
                return '-';
            }
        },
        {
            name: 'recurrence',
            persist: false,
            mapping: function (data) {
                return data.task.recurrence;
            }
        }
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Apr.model.CustomTask',
            associationKey: 'task',
            name: 'task',
            getterName: 'getTask'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ctk/customtask/history',
        reader: {
            type: 'json'
        }
    }
});