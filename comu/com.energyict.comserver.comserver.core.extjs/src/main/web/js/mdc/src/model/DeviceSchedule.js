Ext.define('Mdc.model.DeviceSchedule', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name: 'id'},
        {name: 'masterScheduleId'},
        {name: 'name', type: 'string', useNull: true},
        {name: 'nextExecutionSpecs', useNull: true},
        {name: 'status', useNull: true},
        {name: 'schedule', useNull: true},
        {name: 'plannedDate', dateFormat: 'time', type: 'date'},
        {name: 'nextCommunication', dateFormat: 'time', type: 'date'},
        {name: 'comTaskInfos'},
        {name: 'type', type: 'string'}
    ],
    associations: [
        {name: 'comTaskInfos', type: 'hasMany', model: 'Mdc.model.ComTask', associationKey: 'comTaskInfos', foreignKey: 'comTaskInfos',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.ComTask';
            }
        }
    ]
});