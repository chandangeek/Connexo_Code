Ext.define('Mdc.model.ProtocolDialect', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'availableForUse', type: 'boolean', useNull: true}

    ],
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Mdc.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/protocoldialects'
    }
});