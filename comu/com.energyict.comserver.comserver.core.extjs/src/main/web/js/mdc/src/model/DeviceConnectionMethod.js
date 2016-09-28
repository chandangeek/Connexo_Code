Ext.define('Mdc.model.DeviceConnectionMethod', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'status', type: 'string'},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'displayDirection', type: 'string', useNull: true},
        {name: 'numberOfSimultaneousConnections', type: 'int'},
        {name: 'isDefault', type: 'boolean', useNull: true},
        {name: 'comPortPool', type: 'string', useNull: true},
        {name: 'connectionType', type: 'string', useNull: true},
        {name: 'connectionStrategy', type: 'string', useNull: true},
        'rescheduleRetryDelay',
        {name: 'nextExecutionSpecs', useNull: true, defaultValue: null},
        {name: 'comWindowStart',useNull: true, defaultValue: null},
        {name: 'comWindowEnd',useNull: true, defaultValue: null},
        {
            name: 'connectionWindow',
            persist: false,
            mapping: function (data) {
                return {start: data.comWindowStart, end: data.comWindowEnd};
            }
        }

    ],
    associations: [
        {name: 'rescheduleRetryDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'rescheduleRetryDelay'},
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods'
    }
});