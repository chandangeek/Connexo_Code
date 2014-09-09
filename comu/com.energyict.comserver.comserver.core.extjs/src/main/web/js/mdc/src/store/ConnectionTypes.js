Ext.define('Mdc.store.ConnectionTypes', {
    extend: 'Ext.data.Store',
//    autoLoad: true,
    requires: [
        'Mdc.model.ConnectionType'
    ],
    model: 'Mdc.model.ConnectionType',
    storeId: 'ConnectionTypes',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicecommunicationprotocols/{protocolId}/connectiontypes',
        reader: {
            type: 'json'
        }
    }

});
