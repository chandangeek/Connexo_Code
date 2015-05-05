Ext.define('Mdc.store.DeviceLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceLifeCycle',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        timeout: 100000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceLifeCycles'
        }
    }
});

