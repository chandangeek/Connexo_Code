Ext.define('Dsh.model.ConnectionTask', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'auto'},
        { name: 'device', type: 'auto' },
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
                return data.connectionMethod.name + ' on ' + data.device.name
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
    ]
});
