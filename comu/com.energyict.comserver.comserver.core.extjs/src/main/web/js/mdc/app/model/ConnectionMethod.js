Ext.define('Mdc.model.ConnectionMethod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'allowSimultaneousConnections', type: 'string', useNull: true},
        {name: 'isDefault', type: 'string', useNull: true},
        {name: 'portPool', type: 'string', useNull: true},
        {name: 'connectionType', type: 'string', useNull: true},
        {name: 'host', type: 'string', useNull: true},
        {name: 'portNumber', type: 'string', useNull: true},
        {name: 'connectionTimeout', type: 'string', useNull: true},
        {name: 'comPortPool', type: 'string', useNull: true},
        {name: 'connectionStrategy', type: 'string', useNull: true}
    ],
    associations: [
        {name: 'propertyInfos', type: 'hasMany', model: 'Mdc.model.Property', associationKey: 'propertyInfos', foreignKey: 'propertyInfos',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods'
    }
});