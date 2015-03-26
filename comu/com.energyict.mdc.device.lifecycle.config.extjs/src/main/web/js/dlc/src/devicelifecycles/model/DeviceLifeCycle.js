Ext.define('Dlc.devicelifecycles.model.DeviceLifeCycle', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        reader: {
            type: 'json'            
        }
    }
});
