Ext.define('Mdc.store.DeviceLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DeviceLifeCycle',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles',
        timeout: 300000,
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceLifeCycles'
        }
    }
});

