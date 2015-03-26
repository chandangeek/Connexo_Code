Ext.define('Dlc.devicelifecycles.store.DeviceLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycles.model.DeviceLifeCycle',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceLifeCycles'
        }
    }
});