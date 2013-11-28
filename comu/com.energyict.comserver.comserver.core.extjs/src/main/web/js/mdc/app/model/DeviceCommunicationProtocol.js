Ext.define('Mdc.model.DeviceCommunicationProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'deviceProtocolVersion'
    ],
    associations: [
        {name: 'licensedProtocol', type: 'hasOne', model: 'Mdc.model.LicensedProtocol', associationKey: 'licensedProtocol',
            getterName: 'getLicensedProtocol', setterName: 'setLicensedProtocol', foreignKey: 'licensedProtocol'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicecommunicationprotocols',
        reader: {
            type: 'json'
        }
    }
})
;