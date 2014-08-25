Ext.define('Dsh.model.CommunicationTask', {
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
        { name: 'startTime', type: 'date', dateFormat: 'time'},
        { name: 'successfulFinishTime', type: 'date', dateFormat: 'time'},
        { name: 'nextCommunication', type: 'date', dateFormat: 'time'},
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
    ]
});






