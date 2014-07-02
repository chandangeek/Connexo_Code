Ext.define('Mdc.model.DeviceConnectionMethod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'status', type: 'string'},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'allowSimultaneousConnections', type: 'boolean', useNull: true},
        {name: 'isDefault', type: 'boolean', useNull: true},
        {name: 'comPortPool', type: 'string', useNull: true},
        {name: 'connectionType', type: 'string', useNull: true},
        {name: 'connectionStrategy', type: 'string', useNull: true},
        'rescheduleRetryDelay',
        {name: 'nextExecutionSpecs', useNull: true},
        {name: 'comWindowStart',useNull: true},
        {name: 'comWindowEnd',useNull: true}
    ],
    associations: [
        {name: 'rescheduleRetryDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'rescheduleRetryDelay'},
        {name: 'properties', type: 'hasMany', model: 'Mdc.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mrid}/connectionmethods'
    }
});