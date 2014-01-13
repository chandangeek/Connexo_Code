Ext.define('Mdc.model.DeviceCommunicationProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'deviceProtocolVersion'
    ],
    associations: [
        {name: 'licensedProtocol', type: 'hasOne', model: 'Mdc.model.LicensedProtocol', associationKey: 'licensedProtocol',
            getterName: 'getLicensedProtocol', setterName: 'setLicensedProtocol', foreignKey: 'licensedProtocol'},
        {name: 'propertyInfos', type: 'hasMany', model: 'Mdc.model.Property', associationKey: 'propertyInfos', foreignKey: 'propertyInfos',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Property';
            }
        }
    ],
    idProperty: 'id',
    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicecommunicationprotocols',
        reader: {
            type: 'json'
        }
    }
});