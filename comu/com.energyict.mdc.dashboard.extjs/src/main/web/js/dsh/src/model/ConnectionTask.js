/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.ConnectionTask', {
    extend: 'Uni.model.Version',
    fields: [
        { name: 'id', type: 'auto'},
        { name: 'device', type: 'auto', defaultValue: null},
        { name: 'deviceConfiguration', type: 'auto' },
        { name: 'deviceType', type: 'auto' },
        {
            name: 'devConfig',
            persist: false,
            mapping: function (data) {
                var devConfig = {};
                devConfig.config = data.deviceConfiguration;
                devConfig.devType = data.deviceType;
                return devConfig
            }
        },
        {
            name: 'title',
            persist: false,
            mapping: function (data) {
                return Uni.I18n.translate('general.XonY', 'DSH', '{0} on {1}', [data.connectionMethod.name, data.device.name]);
            }
        },
        { name: 'currentState', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResult', type: 'auto' },
        { name: 'taskCount', type: 'auto' },
        { name: 'startDateTime', type: 'date', dateFormat: 'time'},
        { name: 'endDateTime', type: 'date', dateFormat: 'time'},
        { name: 'duration', type: 'auto' },
        { name: 'comPortPool', type: 'auto' },
        { name: 'direction', type: 'auto' },
        { name: 'connectionType', type: 'auto' },
        { name: 'comServer', type: 'auto' },
        { name: 'connectionMethod', type: 'auto' },
        { name: 'window', type: 'auto' },
        { name: 'connectionStrategy', type: 'auto' },
        { name: 'nextExecution', type: 'date', dateFormat: 'time'},
        { name: 'comPort', type: 'auto'},
        { name: 'comSessionId', type: 'auto'}
    ],

    run: function(callback) {
        var me = this;

        Ext.Ajax.request({
            isNotEdit: true,
            method: 'PUT',
            jsonData: _.pick(me.getRecordData(), 'id', 'device', 'version'),
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            success: callback
        });
    },

    proxy: {
        type: 'rest',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    }

});
