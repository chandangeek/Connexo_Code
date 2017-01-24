Ext.define('Mdc.model.DeviceCommunicationTaskHistory', {
    requires: [
        'Mdc.model.DeviceConnectionHistory'
    ],
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'comTasks'},
        {name: 'device'},
        {name: 'deviceConfiguration'},
        {name: 'deviceType'},
        {name: 'comScheduleName', type: 'string'},
        {name: 'comScheduleFrequency'},
        {name: 'urgency', type: 'int'},
        {name: 'result', type: 'string'},
        {name: 'startTime', dateFormat: 'time', type: 'date'},
        {name: 'finishTime', dateFormat: 'time', type: 'date'},
        {name: 'durationInSeconds', type: 'int'},
        {name: 'alwaysExecuteOnInbound', type: 'string'},
        'comSession'
    ],
    associations: [
        {name: 'comSession',type: 'hasOne',model:'Mdc.model.DeviceConnectionHistory',associationKey: 'comSession',foreignKey: 'comSession',
            getterName: 'getComSession', setterName: 'setComSession'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/comtasks/{comTaskId}/comtaskexecutionsessions'
    }
});
