Ext.define('Mdc.store.DeviceCommunicationProtocols', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceCommunicationProtocol'
    ],
    autoLoad: true,
    model: 'Mdc.model.DeviceCommunicationProtocol',
    storeId: 'DeviceCommunicationProtocols',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicecommunicationprotocols',
        reader: {
            type: 'json',
            root: 'DeviceProtocolPluggableClass'
        }
    }

});