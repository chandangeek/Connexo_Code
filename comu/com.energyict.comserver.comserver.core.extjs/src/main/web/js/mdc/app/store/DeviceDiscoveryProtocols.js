Ext.define('Mdc.store.DeviceDiscoveryProtocols', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceDiscoveryProtocol'
    ],
    autoLoad: true,
    model: 'Mdc.model.DeviceDiscoveryProtocol',
    storeId: 'DeviceDiscoveryProtocols',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicediscoveryprotocols',
        reader: {
            type: 'json',
            root: 'InboundDeviceProtocolPluggableClass'
        }
    }

});