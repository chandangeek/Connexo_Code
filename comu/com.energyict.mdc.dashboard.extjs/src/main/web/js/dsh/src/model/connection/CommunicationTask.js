/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.connection.CommunicationTask', {
    extend: 'Ext.data.Model',
    fields: [
        "name",
        "device",
        "id",
        "deviceConfiguration",
        "deviceType",
        "comScheduleName",
        "comScheduleFrequency",
        "urgency",
        "currentState",
        "alwaysExecuteOnInbound",
        "result",
        "connectionTask",
        "sessionId",
        "comTask",
        { name: 'startTime', type: 'date', dateFormat: 'time'},
        { name: 'stopTime', type: 'date', dateFormat: 'time'},
        { name: 'nextCommunication', type: 'date', dateFormat: 'time'},
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return data.comTask.name + ' on ' + data.device.name;
            }
        },
        {
            name: 'devConfig',
            persist: false,
            mapping: function (data) {
                var devConfig = {};
                devConfig.config = data.deviceConfiguration;
                devConfig.devType = data.deviceType;
                return devConfig
            }
        }
    ],
    hasOne: {
        model:'Dsh.model.ConnectionTask',
        associationKey: 'connectionTask',
        name: 'connectionTask',
        getterName: 'getConnectionTask'
    },

    run: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            success: callback
        });
    },

    runNow: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/runnow'.replace('{id}', this.getId()),
            success: callback
        });
    },

    proxy: {
        type: 'ajax',
        url: '/api/dsr/communications',
        reader: {
            type: 'json',
            root: 'communicationTasks',
            totalProperty: 'total'
        }
    }


});






