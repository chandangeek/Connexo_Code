Ext.define('Mdc.store.DeviceCommunicationProtocols', {
    extend: 'Ext.data.Store',
//    autoLoad: true,
    requires: [
        'Mdc.model.DeviceCommunicationProtocol'
    ],
    model: 'Mdc.model.DeviceCommunicationProtocol',
    storeId: 'DeviceCommunicationProtocols',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        limitParam: false,
        pageParam: false,
        startParam: false,
        url: '../../api/plr/devicecommunicationprotocols',
        reader: {
            type: 'json',
            root: 'deviceCommunicationProtocolInfos'
        }
    }

});