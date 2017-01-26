Ext.define('Mdc.model.DeviceDiscoveryProtocol', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'javaClassName',
        'deviceProtocolVersion',
        'properties'
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicediscoveryprotocols',
        reader: {
            type: 'json'
        }
    },
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
})
;