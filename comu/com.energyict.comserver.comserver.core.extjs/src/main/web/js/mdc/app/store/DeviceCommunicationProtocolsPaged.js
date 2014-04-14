Ext.define('Mdc.store.DeviceCommunicationProtocolsPaged', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceCommunicationProtocol'
    ],
    model: 'Mdc.model.DeviceCommunicationProtocol',
    storeId: 'DeviceCommunicationProtocols',
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicecommunicationprotocols',
        reader: {
            type: 'json',
            root: 'deviceCommunicationProtocolInfos'
        }
    }

});