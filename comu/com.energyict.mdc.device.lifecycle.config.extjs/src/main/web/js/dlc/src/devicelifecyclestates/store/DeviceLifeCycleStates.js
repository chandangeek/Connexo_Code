Ext.define('Dlc.devicelifecyclestates.store.DeviceLifeCycleStates', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecyclestates.model.DeviceLifeCycleState',
    autoLoad: false,
    remoteSort: true,
    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]
});