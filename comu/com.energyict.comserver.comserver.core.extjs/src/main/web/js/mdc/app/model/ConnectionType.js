Ext.define('Mdc.model.ConnectionType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},

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
        url: '../../api/plr/devicecommunicationprotocols/{protocolId}/connectiontypes'
    }
});