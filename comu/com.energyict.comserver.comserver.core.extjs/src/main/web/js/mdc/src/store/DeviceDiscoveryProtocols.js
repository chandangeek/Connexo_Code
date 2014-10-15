Ext.define('Mdc.store.DeviceDiscoveryProtocols', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceDiscoveryProtocol'
    ],
    model: 'Mdc.model.DeviceDiscoveryProtocol',
    storeId: 'DeviceDiscoveryProtocols',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicediscoveryprotocols',
        reader: {
            type: 'json',
            root: 'InboundDeviceProtocolPluggableClass'
        }
    }

});