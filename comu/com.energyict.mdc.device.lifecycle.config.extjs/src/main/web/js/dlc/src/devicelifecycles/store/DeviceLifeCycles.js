Ext.define('Dlc.devicelifecycles.store.DeviceLifeCycles', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecycles.model.DeviceLifeCycle',
    data: [
        {name: 'Standard device life cycle'},
        {name: 'Short device life cycle'},
        {name: 'Extended device life cycle'}
    ]
});