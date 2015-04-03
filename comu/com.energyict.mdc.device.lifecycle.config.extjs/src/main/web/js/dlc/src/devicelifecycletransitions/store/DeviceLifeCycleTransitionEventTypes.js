Ext.define('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionEventTypes', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycletransitions.model.DeviceLifeCycleTransitionEventType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/eventtypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'eventTypes'
        }
    }
});
