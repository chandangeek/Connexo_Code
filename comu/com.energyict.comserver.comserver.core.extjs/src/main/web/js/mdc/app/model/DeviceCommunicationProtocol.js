Ext.define('Mdc.model.DeviceCommunicationProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'databaseId', type: 'int', useNull: true},
        'name',
        'javaClassName'
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicecommunicationprotocols',
        reader: {
            type: 'json',
            root: 'deviceCommunicationProtocols'
        }
    }
});