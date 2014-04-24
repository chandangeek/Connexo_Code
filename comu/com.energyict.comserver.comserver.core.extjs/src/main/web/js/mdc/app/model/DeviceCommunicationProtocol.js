Ext.define('Mdc.model.DeviceCommunicationProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'deviceProtocolVersion'
    ],
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Mdc.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Property';
            }
        }
    ],
    idProperty: 'id',
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicecommunicationprotocols',
        reader: {
            type: 'json'
        }
    }
});