Ext.define('Mdc.model.DeviceDiscoveryProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'javaClassName',
        'deviceProtocolVersion'
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicediscoveryprotocols',
        reader: {
            type: 'json'
        }
    }
})
;